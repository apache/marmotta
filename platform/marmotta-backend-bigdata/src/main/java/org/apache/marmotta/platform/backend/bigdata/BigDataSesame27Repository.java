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

package org.apache.marmotta.platform.backend.bigdata;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.store.AbstractTripleStore;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.*;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.openrdf.sail.helpers.SailWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper to allow using BigData in a modular Sesame 2.7 environment. Provides a fast lane to the BigData SPARQL
 * implementation.
 * <p/>
 * The implementation essentially wraps the BigdataSailRepository provided by BigData, but also allows using a
 * sail stack.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class BigDataSesame27Repository extends SailRepository {

    private static Logger log = LoggerFactory.getLogger(BigDataSesame27Repository.class);

    private Sail wrapped;

    public BigDataSesame27Repository(Sail sail) {
        super(sail);
    }

    /**
     * Return the wrapped bigdata sail, even if it is somewhere deep in the stack
     * @return
     */
    private BigdataSail getBaseSail() {
        Sail sail = wrapped;
        while(sail instanceof SailWrapper) {
            sail = ((SailWrapper) sail).getBaseSail();
        }
        if(sail instanceof BigDataSesame27Sail) {
            return ((BigDataSesame27Sail) sail).getWrapped();
        }
        if(sail instanceof BigdataSail) {
            return (BigdataSail) sail;
        }
        return null;
    }

    private BigdataSail.BigdataSailConnection getBaseSailConnection(SailConnection con) {
        SailConnection wrapped = con;
        while(wrapped instanceof SailConnectionWrapper) {
            wrapped = ((SailConnectionWrapper) wrapped).getWrappedConnection();
        }
        if(wrapped instanceof BigDataSesame27Sail.BigDataSesame27SailConnection) {
            return ((BigDataSesame27Sail.BigDataSesame27SailConnection) wrapped).getWrapped();
        }
        if(wrapped instanceof BigdataSail.BigdataSailConnection) {
            return (BigdataSail.BigdataSailConnection) wrapped;
        }
        return null;
    }

    /**
     * Direct access to the Bigdata database (for SPARQL query evaluation).
     *
     * @return
     */
    public AbstractTripleStore getDatabase() {
        return getBaseSail().getDatabase();
    }


    /**
     * Return a custom version of a repository connection with special SPARQL optimizations.
     * Similar to {@link com.bigdata.rdf.sail.BigdataSailRepositoryConnection}, but a nicer citizen
     * in a sail stack.
     *
     * @return
     * @throws RepositoryException
     */
    @Override
    public SailRepositoryConnection getConnection() throws RepositoryException {
        try {
            final SailConnection con = getSail().getConnection();

            // a wrapper so we can access the SPARQL implementations without needing to replicate their code
            final BigdataSailRepositoryConnection wrapped = new BigdataSailRepositoryConnection(null,getBaseSailConnection(con));

            return new SailRepositoryConnection(this, con) {


                @Override
                public SailBooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString, String baseURI) throws MalformedQueryException {
                    return wrapped.prepareBooleanQuery(ql, queryString, baseURI);
                }

                @Override
                public SailGraphQuery prepareGraphQuery(QueryLanguage ql, String queryString, String baseURI) throws MalformedQueryException {
                    return wrapped.prepareGraphQuery(ql, queryString, baseURI);
                }

                @Override
                public SailQuery prepareQuery(QueryLanguage ql, String queryString, String baseURI) throws MalformedQueryException {
                    return wrapped.prepareQuery(ql,queryString,baseURI);
                }

                @Override
                public SailTupleQuery prepareTupleQuery(QueryLanguage ql, String queryString, String baseURI) throws MalformedQueryException {
                    return wrapped.prepareTupleQuery(ql, queryString, baseURI);
                }

                @Override
                public Update prepareUpdate(QueryLanguage ql, String update, String baseURI) throws RepositoryException, MalformedQueryException {
                    return wrapped.prepareUpdate(ql, update, baseURI);
                }
            };
        } catch (SailException e) {
            throw new RepositoryException("could not create repository connection",e);
        }
    }
}