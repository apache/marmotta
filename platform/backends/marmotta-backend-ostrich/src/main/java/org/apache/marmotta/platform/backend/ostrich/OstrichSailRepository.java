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

package org.apache.marmotta.platform.backend.ostrich;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.ostrich.sail.OstrichSailConnection;
import org.openrdf.query.*;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailQuery;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.sail.SailTupleQuery;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A wrapper SailRepository for Ostrich allowing access to direct SPARQL support.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class OstrichSailRepository extends SailRepository {
    private static Logger log = LoggerFactory.getLogger(OstrichSailRepository.class);

    public OstrichSailRepository(Sail sail) {
        super(sail);
    }

    @Override
    public SailRepositoryConnection getConnection() throws RepositoryException {
        try {
            final SailConnection con = getSail().getConnection();

            return new SailRepositoryConnection(this, con) {
                @Override
                public SailTupleQuery prepareTupleQuery(final QueryLanguage ql, final String queryString, final String baseURI) throws MalformedQueryException {
                    if (ql == QueryLanguage.SPARQL) {
                        return new SailTupleQuery(null, this) {
                            @Override
                            public TupleQueryResult evaluate() throws QueryEvaluationException {
                                try {
                                    log.info("Running native SPARQL query: {}", queryString);
                                    CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter;

                                    // Let Sesame still parse the query for better error messages and for the binding names.
                                    ParsedTupleQuery parsedQuery = QueryParserUtil.parseTupleQuery(ql, queryString, baseURI);
                                    OstrichSailConnection sailCon = findConnection(getConnection().getSailConnection());
                                    bindingsIter = sailCon.directTupleQuery(queryString);
                                    bindingsIter = enforceMaxQueryTime(bindingsIter);

                                    return new TupleQueryResultImpl(new ArrayList<String>(parsedQuery.getTupleExpr().getBindingNames()), bindingsIter);
                                } catch (SailException e) {
                                    throw new QueryEvaluationException(e.getMessage(), e);
                                } catch (MalformedQueryException e) {
                                    throw new QueryEvaluationException(e.getMessage(), e);
                                }
                            }
                        };
                    } else {
                        return super.prepareTupleQuery(ql, queryString, baseURI);
                    }
                }

                @Override
                public SailQuery prepareQuery(QueryLanguage ql, String queryString, String baseURI) throws MalformedQueryException {
                    ParsedQuery parsedQuery = QueryParserUtil.parseQuery(ql, queryString, baseURI);

                    if (parsedQuery instanceof ParsedTupleQuery) {
                        return prepareTupleQuery(ql, queryString, baseURI);
                    } else {
                        return super.prepareQuery(ql, queryString, baseURI);
                    }
                }
            };
        } catch (SailException e) {
            throw new RepositoryException("could not create repository connection",e);
        }
    }

    private static OstrichSailConnection findConnection(SailConnection current) {
        if (current instanceof OstrichSailConnection) {
            return (OstrichSailConnection)current;
        } else if (current instanceof SailConnectionWrapper) {
            return findConnection(((SailConnectionWrapper) current).getWrappedConnection());
        }
        return null;
    }
}
