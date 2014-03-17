/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.kiwi.persistence;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.BloomFilter;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.sesame.hashing.URIFunnel;
import org.apache.marmotta.kiwi.exception.DriverNotFoundException;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.FN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

/**
 * A dialect provides the SQL statements necessary to access the different types of database systems. Each
 * method should return a PreparedStatement that can be executed on the respective JDBC connection
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class KiWiDialect {

    private static Logger log = LoggerFactory.getLogger(KiWiDialect.class);

    private final static int VERSION = 2;
    protected BloomFilter<URI> supportedFunctions;

    private Properties statements;


    protected KiWiDialect() throws DriverNotFoundException {
        statements = new Properties();

        // load all statements.properties files that can be located in the same package (from different modules in different jar files)
        try {
            Enumeration<URL> urls = this.getClass().getClassLoader().getResources(this.getClass().getPackage().getName().replace('.','/')+"/statements.properties");
            while(urls.hasMoreElements()) {
                statements.load(urls.nextElement().openStream());
            }
        } catch (Exception e) {
            log.error("could not load statement definitions (statement.properties)",e);
        }

        supportedFunctions = BloomFilter.create(URIFunnel.getInstance(), 1000);
    }

    public int getVersion() {
        return VERSION;
    }


    /**
     * Return the name of the driver class (used for properly initialising JDBC connections)
     * @return
     */
    public abstract String getDriverClass();


    /**
     * Return true if batched commits are supported by this dialect.
     * @return
     */
    public abstract boolean isBatchSupported();

    /**
     * Return the contents of the SQL create script used for initialising an empty database
     * @return
     */
    public String getCreateScript(String scriptName) {
        return getScript("create_"+scriptName+"_tables.sql");
    }


    /**
     * Return the contents of the SQL drop script used for cleaning up all traces of the KiWi triple store
     * @return
     */
    public String getDropScript(String scriptName) {
        return getScript("drop_"+scriptName+"_tables.sql");
    }


    /**
     * Return the contents of the SQL script with the given file name (relative to the current class)
     * @return
     */
    protected String getScript(String scriptName) {
        try {
            return IOUtils.toString(this.getClass().getResourceAsStream(scriptName));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Return the contents of the SQL scripts used for migrating from the version given as argument to the
     * current version. In case the update covers several version steps, this method should concatenate the
     * migration scripts in proper order.
     *
     * @param oldVersion the version to migrate the database from
     * @param name       name of the script to create; the method will look for scripts with name upgrade_name_oldv_newv.sql
     * @return the migration script from the old version to the current version of the database schema, or null
     *         if the database is already the current version
     */
    public String getMigrationScript(int oldVersion, String name) {
        StringBuilder builder = new StringBuilder();
        for(int i = oldVersion+1; i <= VERSION; i++ ) {
            try {
                String script = String.format("upgrade_"+name+"_%03d_%03d.sql",i-1,i);

                builder.append(IOUtils.toString(this.getClass().getResourceAsStream(script)));
            } catch (Exception e) {
                log.warn("upgrade script {} -> {} not found or not readable!", i-1, i);
            }
        }
        return builder.toString();
    }


    /**
     * Return the SQL statement with the given identifier, or null if such a statement does not exist. The
     * statement identifiers are usually the keys in the statements.properties file for the respective database.
     *
     * @param identifier property key of the statement in statements.properties
     * @return the SQL statement, or null if it does not exist
     */
    public String getStatement(String identifier) {
        return statements.getProperty(identifier);
    }

    /**
     * Return true if a statement with the given identifier exists
     * @param identifier key of the statement to check
     * @return true if a statement with the given identifier exists
     */
    public boolean hasStatement(String identifier) {
        return statements.getProperty(identifier) != null;
    }

    /**
     * Return all available statement identifiers. The statement identifiers are usually the keys in the
     * statements.properties file for the respective database.
     *
     * @return a set of all statement identifiers
     */
    public Set<String> getStatementIdentifiers() {
        return statements.stringPropertyNames();
    }

    /**
     * Return the names of all sequences that have been configured in the system, i.e. all statements starting with "seq."
     * @return
     */
    public Set<String> listSequences(String scriptName) {
        // quick hack for current modules, fix later!
        if("base".equals(scriptName)) {
            return ImmutableSet.of("seq.nodes", "seq.triples", "seq.namespaces");
        } else if("reasoner".equals(scriptName)) {
            return ImmutableSet.of("seq.rules", "seq.justifications", "seq.programs");
        } else if("versioning".equals(scriptName)) {
            return ImmutableSet.of("seq.versions");
        } else if("ldcache".equals(scriptName)) {
            return ImmutableSet.of("seq.ldcache");
        } else {
            return Collections.emptySet();
        }


        /*
        Set<String> names = new HashSet<String>();
        Enumeration e = statements.propertyNames();
        while(e.hasMoreElements()) {
            String[] keys = e.nextElement().toString().split("\\.");
            if(keys[0].equals("seq")) {
                names.add(keys[0] + "." + keys[1]);
            }
        }
        return names;
        */
    }

    /**
     * Return the database specific operator for matching a text against a regular expression.
     *
     *
     * @param text
     * @param pattern
     * @param flags
     * @return
     */
    public abstract String getRegexp(String text, String pattern, String flags);


    /**
     * Return true in case the SPARQL RE flags contained in the given string are supported.
     *
     * @param flags
     * @return
     */
    public abstract boolean isRegexpSupported(String flags);

    /**
     * Return the database specific case insensitive like comparison, e.g. ILIKE in Postgres.
     *
     * @param text
     * @param pattern
     * @return
     */
    public abstract String getILike(String text, String pattern);


    /**
     * Get the database specific string concatenation function for the (variable number of) arguments.
     *
     * @param args
     * @deprecated use {@link #getFunction(URI, String...)}  instead
     * @return
     */
    @Deprecated
    public String getConcat(String... args) {
        return getFunction(FN.CONCAT, args);
    }


    /**
     * Get the query string that can be used for validating that a JDBC connection to this database is still valid.
     * Typically, this should be an inexpensive operation like "SELECT 1",
     * @return
     */
    public abstract String getValidationQuery();

    /**
     * Return an SQL string for evaluating the function with the given URI on the arguments. All arguments are already
     * properly substituted SQL expressions (e.g. field names or constants).
     * <p/>
     * Dialects should at least implement support for all functions defined in the SPARQL specification (http://www.w3.org/TR/sparql11-query/#SparqlOps)
     *
     * @param fnUri
     * @param args
     * @throws UnsupportedOperationException in case the function with the given URI is not supported
     * @return
     */
    public abstract String getFunction(URI fnUri, String... args);


    /**
     * Return true in case the database dialect offers direct support for the function with the URI given as argument.
     * This method is used to determine whether a SPARQL query containing a function can be optimized or needs to be
     * evaluated in-memory.
     *
     * @param fnUri
     * @return
     */
    public boolean isFunctionSupported(URI fnUri) {
        return supportedFunctions.mightContain(fnUri);
    }

    /**
     * Return true in case the database system supports using cursors for queries over large data tables.
     * @return
     */
    public boolean isCursorSupported() {
        return false;
    }
}
