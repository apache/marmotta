/*
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

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.kiwi.exception.DriverNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
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

    public final static int VERSION = 4;

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
     * Return true in case the database supports creating arrays with ARRAY[...]
     * @return
     */
    public boolean isArraySupported() {
        return false;
    }

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
     * Return the name of the aggregate function for group concatenation (string_agg in postgres, GROUP_CONCAT in MySQL)
     * @return
     */
    public abstract String getGroupConcat(String value, String separator, boolean distinct);


    /**
     * Return the SQL timezone value for a KiWiDateLiteral, corrected by the timezone offset. In PostgreSQL, this is
     * e.g. computed by (ALIAS.tvalue + ALIAS.tzoffset * INTERVAL '1 second')
     * @param alias
     * @return
     */
    public abstract String getDateTimeTZ(String alias);

    /**
     * Get the query string that can be used for validating that a JDBC connection to this database is still valid.
     * Typically, this should be an inexpensive operation like "SELECT 1",
     * @return
     */
    public abstract String getValidationQuery();


    /**
     * Return true in case the database system supports using cursors for queries over large data tables.
     * @return
     */
    public boolean isCursorSupported() {
        return false;
    }
}
