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

package org.apache.marmotta.kiwi.test.profile;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An executable class that can be used for profiling purposes. It requires a JDBC connection string to
 * an existing (filled) database for running.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ProfileLoading {

    private static Logger log = LoggerFactory.getLogger(ProfileLoading.class);

    private KiWiStore store;

    private Repository repository;


    public ProfileLoading(String jdbcUrl, String user, String password) throws RepositoryException {
        this(new KiWiConfiguration("profiling",jdbcUrl,user,password, new PostgreSQLDialect()));
    }

    public ProfileLoading(KiWiConfiguration cfg) throws RepositoryException {
        store = new KiWiStore(cfg);
        repository = new SailRepository(store);
        repository.initialize();
    }


    public void profileListStatements() throws RepositoryException {
        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();

            long start = System.currentTimeMillis();
            long stmts = 0;

            RepositoryResult<Statement> r = con.getStatements(null,null,null,true);
            while(r.hasNext()) {
                Statement s = r.next();
                stmts ++;
            }

            long end = System.currentTimeMillis();

            log.info("listed {} triples in {} ms", stmts, end-start);


            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }

    }


    public void shutdown() throws RepositoryException {
        repository.shutDown();
    }

    public static void main(String[] args) throws RepositoryException {
        if(args.length != 3) {
            log.error("arguments: <jdbc-url> <user> <password>");
            System.exit(1);
        }

        ProfileLoading l = new ProfileLoading(args[0],args[1],args[2]);
        l.profileListStatements();
        l.shutdown();

    }

}
