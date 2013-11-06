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
import org.apache.marmotta.kiwi.persistence.mysql.MySQLDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.persistence.util.ScriptRunner;
import org.apache.marmotta.kiwi.sail.KiWiSailConnection;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sparql.persistence.KiWiSparqlConnection;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.openrdf.sail.helpers.SailWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

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
        prepareFulltext(this.parent.getPersistence().getConfiguration());
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
                        if(connection.getMetadata("fulltext.langlookup") == null) {
                            runner.runScript(new StringReader(IOUtils.toString(PostgreSQLDialect.class.getResourceAsStream("create_fulltext_langlookup.sql")).replaceAll("\\n"," ")));
                        }

                        if(configuration.getFulltextLanguages() != null) {
                            String script = IOUtils.toString(PostgreSQLDialect.class.getResourceAsStream("create_fulltext_index.sql")).replaceAll("\\n"," ");
                            for(String lang : configuration.getFulltextLanguages()) {
                                if(connection.getMetadata("fulltext.index."+lang) == null) {
                                    String script_lang = script.replaceAll("@LANGUAGE@", lang);
                                    runner.runScript(new StringReader(script_lang));
                                }
                            }
                        }
                    } else if(configuration.getDialect() instanceof MySQLDialect) {

                        // for MySQL, just create a fulltext index (no language support)
                        if(connection.getMetadata("fulltext.index") == null) {
                            ScriptRunner runner = new ScriptRunner(connection.getJDBCConnection(), false, false);
                            String script = IOUtils.toString(MySQLDialect.class.getResourceAsStream("create_fulltext_index.sql"));
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

        try {
            return new KiWiSparqlSailConnection(connection, new KiWiSparqlConnection(root.getDatabaseConnection(), root.getValueFactory()), root.getValueFactory());
        } catch (SQLException e) {
            throw new SailException(e);
        }
    }
}
