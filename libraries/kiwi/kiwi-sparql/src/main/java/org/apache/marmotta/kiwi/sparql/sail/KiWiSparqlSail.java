/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.sparql.sail;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.persistence.util.ScriptRunner;
import org.apache.marmotta.kiwi.sail.KiWiSailConnection;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.sail.*;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.openrdf.sail.helpers.SailWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiSparqlSail extends NotifyingSailWrapper {

    private static Logger log = LoggerFactory.getLogger(KiWiSparqlSail.class);


    private KiWiStore parent;

    public KiWiSparqlSail(NotifyingSail baseSail) {
        super(baseSail);

        this.parent = getRootSail(baseSail);
    }

    @Override
    public void initialize() throws SailException {
        super.initialize();

        // start fulltext indexing in a separate thread (MARMOTTA-415)
        Thread indexer = new Thread("Fulltext Indexer") {
            @Override
            public void run() {
                prepareFulltext(parent.getPersistence().getConfiguration());
            }
        };
        indexer.start();
    }

    /**
     * Get the root sail in the wrapped sail stack
     * @param sail
     * @return
     */
    private KiWiStore getRootSail(Sail sail) {
        if(sail instanceof KiWiStore) {
            return (KiWiStore) sail;
        } else if(sail instanceof SailWrapper) {
            return getRootSail(((SailWrapper) sail).getBaseSail());
        } else {
            throw new IllegalArgumentException("root sail is not a KiWiStore or could not be found");
        }
    }


    private void prepareFulltext(KiWiConfiguration configuration) {
        try {
            if(configuration.isFulltextEnabled()) {
                KiWiConnection connection = parent.getPersistence().getConnection();
                try {
                    if(configuration.getDialect() instanceof PostgreSQLDialect) {

                        // for postgres, we need to create
                        // - a stored procedure for mapping ISO language codes to PostgreSQL fulltext configuration names
                        // - if languages are not null, for each configured language as well as for the generic configuration
                        //   an index over nodes.svalue

                        ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);
                        if(connection.getMetadata("ft.lookup") == null) {
                            log.info("PostgreSQL: creating language configuration lookup function");
                            StringBuilder script = new StringBuilder();
                            for(String line : IOUtils.readLines(PostgreSQLDialect.class.getResourceAsStream("create_fulltext_langlookup.sql"))) {
                                if(!line.startsWith("--")) {
                                    script.append(line);
                                    script.append(" ");
                                }
                            }
                            log.debug("PostgreSQL: running SQL script '{}'", script.toString());
                            runner.runScript(new StringReader(script.toString()));
                        }

                        // language specific indexes
                        if(configuration.getFulltextLanguages() != null) {
                            StringBuilder script = new StringBuilder();
                            for(String line : IOUtils.readLines(PostgreSQLDialect.class.getResourceAsStream("create_fulltext_index.sql"))) {
                                if(!line.startsWith("--")) {
                                    script.append(line);
                                    script.append(" ");
                                }
                            }
                            for(String lang : configuration.getFulltextLanguages()) {
                                if(connection.getMetadata("ft.idx."+lang) == null) {
                                    String pg_configuration = POSTGRES_LANG_MAPPINGS.get(lang);
                                    if(pg_configuration != null) {
                                        log.info("PostgreSQL: creating fulltext index for language {}", lang);
                                        String script_lang = script.toString().replaceAll("@LANGUAGE@", lang).replaceAll("@CONFIGURATION@",pg_configuration);
                                        log.debug("PostgreSQL: running SQL script '{}'", script_lang);
                                        runner.runScript(new StringReader(script_lang));
                                    }
                                }
                            }
                        }

                        // generic index
                        if(configuration.getFulltextLanguages() != null) {
                            if(connection.getMetadata("ft.idx.generic") == null) {
                                StringBuilder script = new StringBuilder();
                                for(String line : IOUtils.readLines(PostgreSQLDialect.class.getResourceAsStream("create_fulltext_index_generic.sql"))) {
                                    if(!line.startsWith("--")) {
                                        script.append(line);
                                        script.append(" ");
                                    }
                                }
                                log.info("PostgreSQL: creating generic fulltext index ");
                                log.debug("PostgreSQL: running SQL script '{}'", script.toString());
                                runner.runScript(new StringReader(script.toString()));
                            }
                        }

                        /*
                    } else if(configuration.getDialect() instanceof MySQLDialect) {

                        // for MySQL, just create a fulltext index (no language support)
                        if(connection.getMetadata("ft.idx") == null) {
                            ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);
                            String script = IOUtils.toString(MySQLDialect.class.getResourceAsStream("create_fulltext_index.sql"));
                            log.info("MySQL: creating generic fulltext index ");
                            log.debug("MySQL: running SQL script '{}'", script.toString());
                            runner.runScript(new StringReader(script));
                        }
                        /*
                    } else if(configuration.getDialect() instanceof H2Dialect) {

                        // for H2, just create a fulltext index (no language support)
                        if(connection.getMetadata("fulltext.index") == null) {
                            ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);
                            String script = IOUtils.toString(H2Dialect.class.getResourceAsStream("create_fulltext_index.sql"));
                            runner.runScript(new StringReader(script));
                        }
                        */
                    }
                } finally {
                    connection.close();
                }
            }
        } catch (IOException | SQLException ex) {
            log.error("error while preparing fulltext support",ex);
        }
    }

    /**
     * Get the root connection in a wrapped sail connection stack
     * @param connection
     * @return
     */
    private KiWiSailConnection getRootConnection(SailConnection connection) {
        if(connection instanceof KiWiSailConnection) {
            return (KiWiSailConnection) connection;
        } else if(connection instanceof SailConnectionWrapper) {
            return getRootConnection(((SailConnectionWrapper) connection).getWrappedConnection());
        } else {
            throw new IllegalArgumentException("root connection is not a KiWiSailConnection or could not be found");
        }
    }

    @Override
    public NotifyingSailConnection getConnection() throws SailException {
        NotifyingSailConnection connection = super.getConnection();
        KiWiSailConnection root   = getRootConnection(connection);

        return new KiWiSparqlSailConnection(connection, root.getDatabaseConnection(), root.getValueFactory());
    }


    private static Map<String,String> POSTGRES_LANG_MAPPINGS = new HashMap<>();
    static {
        POSTGRES_LANG_MAPPINGS.put("en","english");
        POSTGRES_LANG_MAPPINGS.put("de","german");
        POSTGRES_LANG_MAPPINGS.put("fr","french");
        POSTGRES_LANG_MAPPINGS.put("it","italian");
        POSTGRES_LANG_MAPPINGS.put("es","spanish");
        POSTGRES_LANG_MAPPINGS.put("pt","portuguese");
        POSTGRES_LANG_MAPPINGS.put("sv","swedish");
        POSTGRES_LANG_MAPPINGS.put("no","norwegian");
        POSTGRES_LANG_MAPPINGS.put("dk","danish");
        POSTGRES_LANG_MAPPINGS.put("nl","dutch");
        POSTGRES_LANG_MAPPINGS.put("ru","russian");
        POSTGRES_LANG_MAPPINGS.put("tr","turkish");
        POSTGRES_LANG_MAPPINGS.put("hu","hungarian");
        POSTGRES_LANG_MAPPINGS.put("fi","finnish");

    }
}
