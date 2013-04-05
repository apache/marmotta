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
package org.apache.marmotta.platform.sparql.services.sparql;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.platform.sparql.services.sparqlio.rdf.SPARQLGraphResultWriter;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

/**
 * Sparql Service implementation
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
@ApplicationScoped
public class SparqlServiceImpl implements SparqlService {

    /**
     * Get the seam logger for issuing logging statements.
     */
    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;
    
    @Override
    public Query parseQuery(QueryLanguage language, String query) throws RepositoryException, MalformedQueryException {
    	Query sparqlQuery = null;
        RepositoryConnection connection = sesameService.getConnection();
        try {
            connection.begin();
            sparqlQuery = connection.prepareQuery(language, query);
            connection.commit();
        } finally {
            connection.close();
        }
        return sparqlQuery;
    }

    @Override
    public void query(QueryLanguage queryLanguage, String query, TupleQueryResultWriter tupleWriter, BooleanQueryResultWriter booleanWriter, SPARQLGraphResultWriter graphWriter) throws MarmottaException, MalformedQueryException, QueryEvaluationException {
        long start = System.currentTimeMillis();

        log.debug("executing SPARQL query:\n{}", query);

        try {
            RepositoryConnection connection = sesameService.getConnection();
            try {
                connection.begin();
                Query sparqlQuery = connection.prepareQuery(queryLanguage, query);

                if (sparqlQuery instanceof TupleQuery) {
                    query((TupleQuery) sparqlQuery, tupleWriter);
                } else if (sparqlQuery instanceof BooleanQuery) {
                    query((BooleanQuery) sparqlQuery, booleanWriter);
                } else if (sparqlQuery instanceof GraphQuery) {
                    query((GraphQuery) sparqlQuery, graphWriter);
                } else {
                    connection.rollback();
                    throw new InvalidArgumentException("SPARQL query type " + sparqlQuery.getClass() + " not supported!");
                }

                connection.commit();
            } finally {
                connection.close();
            }
        } catch(RepositoryException e) {
            log.error("error while getting repository connection: {}", e);
            throw new MarmottaException("error while getting repository connection", e);
        } catch (QueryEvaluationException e) {
        	log.error("error while evaluating query: {}", e);
            throw new MarmottaException("error while writing query result in format ", e);
        }

        log.debug("SPARQL execution took {}ms", System.currentTimeMillis()-start);
    }
    
    @Override
    public void query(QueryLanguage language, String query, OutputStream output, String format) throws MarmottaException {
        long start = System.currentTimeMillis();

        log.debug("executing SPARQL query:\n{}", query);

        try {
            RepositoryConnection connection = sesameService.getConnection();
            try {
                connection.begin();
                Query sparqlQuery = connection.prepareQuery(language, query);

                if (sparqlQuery instanceof TupleQuery) {
                	query((TupleQuery)sparqlQuery, output, format);
                } else if (sparqlQuery instanceof BooleanQuery) {
                	query((BooleanQuery)sparqlQuery, output, format);
                } else if (sparqlQuery instanceof GraphQuery) {
                	query((GraphQuery)sparqlQuery, output, format);
                } else {
                    throw new InvalidArgumentException("SPARQL query type " + sparqlQuery.getClass() + " not supported!");
                }

                connection.commit();
			} finally {
                connection.close();
            }
        } catch(RepositoryException e) {
            log.error("error while getting repository connection: {}", e);
            throw new MarmottaException("error while getting repository connection", e);
        } catch (QueryEvaluationException e) {
        	log.error("error while evaluating query: {}", e);
            throw new MarmottaException("error while writing query result in format ", e);
        } catch (MalformedQueryException e) {
        	log.error("error because malformed query: {}", e);
            throw new MarmottaException("error because malformed query result in format ", e);
        }

        log.debug("SPARQL execution took {}ms", System.currentTimeMillis()-start);
    }

    private void query(TupleQuery query, TupleQueryResultWriter writer) throws QueryEvaluationException {
    	try {
			query.evaluate(writer);
		} catch (TupleQueryResultHandlerException e) {
			throw new QueryEvaluationException("error while writing query tuple result: ",e);
		}
    }
    
    private void query(TupleQuery query, OutputStream output, String format) throws QueryEvaluationException {
    	query(query, SparqlWritersHelper.getTupleResultWriter(format, output));
    }
    
    private void query(BooleanQuery query, BooleanQueryResultWriter writer) throws QueryEvaluationException {
    	try {
			writer.write(query.evaluate());
		} catch (IOException e) {
			throw new QueryEvaluationException("error while writing query boolean result: ",e);
		}
    }
    
    private void query(BooleanQuery query, OutputStream output, String format) throws QueryEvaluationException {
    	query(query, SparqlWritersHelper.getBooleanResultWriter(format, output));
    }
    
    private void query(GraphQuery query, SPARQLGraphResultWriter writer) throws QueryEvaluationException {
    	try {
			writer.write(query.evaluate());
		} catch (IOException e) {
			throw new QueryEvaluationException("error while writing query graph result: ",e);
		}
    }
    
    private void query(GraphQuery query, OutputStream output, String format) throws QueryEvaluationException {
    	query(query, SparqlWritersHelper.getGraphResultWriter(format, output));
    }
    
    /**
     * Evaluate a SPARQL query on the LMF TripleStore. Returns the results as a list of result maps, each element
     * a KiWiNode.
     * <p/>
     * see http://www.w3.org/TR/sparql11-query/
     *
     * @param queryLanguage the query language to use
     * @param query         the SPARQL query to evaluate in SPARQL 1.1 syntax
     */
    @Override
    public List<Map<String, Value>> query(QueryLanguage queryLanguage, String query) throws MarmottaException {
        long start = System.currentTimeMillis();

        log.debug("executing {} query:\n{}", queryLanguage.getName(), query);

        List<Map<String,Value>> result = new LinkedList<Map<String, Value>>();

        try {
            RepositoryConnection connection = sesameService.getConnection();
            try {
                connection.begin();
                TupleQuery tupleQuery = connection.prepareTupleQuery(queryLanguage, query);
                TupleQueryResult r = tupleQuery.evaluate();
                try {
                    while (r.hasNext()) {
                        BindingSet s = r.next();
                        Map<String, Value> map = new HashMap<String, Value>();
                        for (Binding binding : s) {
                            map.put(binding.getName(), binding.getValue());
                        }
                        result.add(map);
                    }
                } finally {
                    r.close();
                }
                //
                connection.commit();
            } finally {
                connection.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while getting repository connection");
            throw new MarmottaException("error while getting repository connection",ex);
        } catch (QueryEvaluationException e) {
            throw new MarmottaException("error while evaluating SPARQL query "+query,e);
        } catch (MalformedQueryException e) {
            throw new InvalidArgumentException("malformed SPARQL query ("+query+") for language "+queryLanguage,e);
        }

        log.debug("SPARQL execution took {}ms",System.currentTimeMillis()-start);
        return result;
    }

    /**
     * Execute a SPARQL update on the LMF TripleStore. Throws a KiWiException in case the update execution fails.
     * <p/>
     * see http://www.w3.org/TR/sparql11-update/
     *
     * @param queryLanguage
     * @param query         a string representing the update query in SPARQL Update 1.1 syntax
     * @throws Exception
     */
    @Override
    public void update(QueryLanguage queryLanguage, String query) throws MarmottaException {
        long start = System.currentTimeMillis();

        log.debug("executing SPARQL update:\n{}", query);

        try {
            RepositoryConnection connection = sesameService.getConnection();
            try {
                connection.begin();
                Update update = connection.prepareUpdate(queryLanguage,query,configurationService.getBaseUri());
                update.execute();
                connection.commit();
            } catch (UpdateExecutionException e) {
                connection.rollback();
                throw new MarmottaException("error while executing update",e);
            } catch (MalformedQueryException e) {
                connection.rollback();
                throw new MarmottaException("malformed query, update failed",e);
            } finally {
                connection.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while getting repository connection", ex);
            throw new MarmottaException("error while getting repository connection",ex);
        }
        log.debug("SPARQL update execution took {}ms",System.currentTimeMillis()-start);

    }

    @Override
    public boolean ask(QueryLanguage queryLanguage, String query)
            throws MarmottaException {
        long start = System.currentTimeMillis();

        log.debug("executing SPARQL ask:\n{}", query);

        boolean result = false;
        try {
            RepositoryConnection connection = sesameService.getConnection();
            try {
                connection.begin();
                BooleanQuery ask = connection.prepareBooleanQuery(queryLanguage, query);
                result = ask.evaluate();
                connection.commit();
            } catch (MalformedQueryException e) {
                throw new MarmottaException("malformed query, update failed",e);
            } catch (QueryEvaluationException e) {
                throw new MarmottaException("error evaluating querry",e);
            } finally {
                connection.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while getting repository connection", ex);
            throw new MarmottaException("error while getting repository connection",ex);
        }
        log.debug("SPARQL update execution took {}ms",System.currentTimeMillis()-start);
        return result;
    }



}
