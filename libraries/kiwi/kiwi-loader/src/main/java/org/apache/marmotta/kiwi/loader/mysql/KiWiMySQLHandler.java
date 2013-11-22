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

package org.apache.marmotta.kiwi.loader.mysql;

import com.mysql.jdbc.Statement;
import org.apache.marmotta.kiwi.loader.KiWiLoaderConfiguration;
import org.apache.marmotta.kiwi.loader.generic.KiWiBatchHandler;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.rio.RDFHandler;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A fast-lane RDF import handler for MySQL backends. This importer takes advantage of the MySQL LOAD DATA command
 * that allows direct injection into the database. It works by creating an intermediate CSV buffer that is flushed into
 * the databases in batches (using a configurable batch size).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiMySQLHandler extends KiWiBatchHandler implements RDFHandler {

    public KiWiMySQLHandler(KiWiStore store, KiWiLoaderConfiguration config) {
        super("MySQL", store, config);
    }

    /**
     * Create indexes again in the database after importing has finished; needs to be implemented by subclasses and
     * should revert all changes done by dropIndexes()
     *
     * @throws java.sql.SQLException
     */
    @Override
    protected void createIndexes() throws SQLException {
        Statement statement = (com.mysql.jdbc.Statement)connection.getJDBCConnection().createStatement();
        statement.execute("ALTER TABLE nodes ENABLE KEYS");
        statement.execute("ALTER TABLE triples ENABLE KEYS");
        statement.execute("SET UNIQUE_CHECKS=1; ");
    }

    /**
     * Flush the backlog (nodeBacklog and tripleBacklog) to the database; needs to be implemented by subclasses.
     *
     * @throws java.sql.SQLException
     */
    @Override
    protected void flushBacklogInternal() throws SQLException {
        try {
            // load node backlog
            Statement statement = (com.mysql.jdbc.Statement)connection.getJDBCConnection().createStatement();
            statement.setLocalInfileInputStream(MySQLLoadUtil.flushNodes(nodeBacklog));
            statement.execute(
                    "LOAD DATA LOCAL INFILE 'nodes.csv' " +
                            "INTO TABLE nodes " +
                            "COLUMNS TERMINATED BY ',' " +
                            "OPTIONALLY ENCLOSED BY '\"' " +
                            "ESCAPED BY '\"' " +
                            "LINES TERMINATED BY '\\r\\n' " +
                            "(id,ntype,svalue,dvalue,ivalue,tvalue,bvalue,ltype,lang,createdAt)");


            statement.setLocalInfileInputStream(MySQLLoadUtil.flushTriples(tripleBacklog));
            statement.execute(
                    "LOAD DATA LOCAL INFILE 'triples.csv' " +
                            "INTO TABLE triples " +
                            "COLUMNS TERMINATED BY ',' " +
                            "OPTIONALLY ENCLOSED BY '\"' " +
                            "ESCAPED BY '\"' " +
                            "LINES TERMINATED BY '\\r\\n' " +
                            "(id,subject,predicate,object,context,creator,inferred,deleted,createdAt,deletedAt)");

            statement.close();

        } catch (IOException ex) {
            throw new SQLException("error while flushing out data",ex);
        }
    }

    /**
     * Drop indexes in the database to increase import performance; needs to be implemented by subclasses. If this
     * feature is not supported, can be an empty method.
     *
     * @throws java.sql.SQLException
     */
    @Override
    protected void dropIndexes() throws SQLException {
        Statement statement = (com.mysql.jdbc.Statement)connection.getJDBCConnection().createStatement();
        statement.execute("SET UNIQUE_CHECKS=0; ");
        statement.execute("ALTER TABLE nodes DISABLE KEYS");
        statement.execute("ALTER TABLE triples DISABLE KEYS");
    }
}
