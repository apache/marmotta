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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
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
 * @author sschaffe
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
    public void query(QueryLanguage queryLanguage, String query, TupleQueryResultWriter tupleWriter, BooleanQueryResultWriter booleanWriter, SPARQLGraphResultWriter graphWriter) throws MarmottaException, MalformedQueryException, QueryEvaluationException {
        long start = System.currentTimeMillis();

        log.debug("executing SPARQL query:\n{}", query);

        try {
            RepositoryConnection connection = sesameService.getConnection();
            try {
                connection.begin();
                Query sparqlQuery = connection.prepareQuery(queryLanguage, query);

                if (sparqlQuery instanceof TupleQuery) {
                    TupleQuery tupleQuery = (TupleQuery) sparqlQuery;
                    tupleQuery.evaluate(tupleWriter);
                } else if (sparqlQuery instanceof BooleanQuery) {
                    BooleanQuery booleanQuery = (BooleanQuery) sparqlQuery;
                    booleanWriter.write(booleanQuery.evaluate());
                } else if (sparqlQuery instanceof GraphQuery) {
                    GraphQuery graphQuery = (GraphQuery) sparqlQuery;
                    graphWriter.write(graphQuery.evaluate());
                } else {
                    connection.rollback();
                    throw new InvalidArgumentException("SPARQL query type " + sparqlQuery.getClass() + " not supported!");
                }

                connection.commit();
            } finally {
                connection.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while getting repository connection");
            throw new MarmottaException("error while getting repository connection",ex);
        } catch (TupleQueryResultHandlerException e) {
            throw new MarmottaException("error while writing query result in format ",e);
        } catch (IOException e) {
            throw new MarmottaException("error while writing query result in format ",e);
        }

        log.debug("SPARQL execution took {}ms",System.currentTimeMillis()-start);
    }

//    private static Pattern subTypePattern = Pattern.compile("[a-z]+/([a-z0-9-._]+\\+)?([a-z0-9-._]+)(;.*)?");
//    private String parseSubType(String mimeType) {
//        Matcher matcher = subTypePattern.matcher(mimeType);
//        if(matcher.matches()) return matcher.group(2);
//        else
//            return mimeType;
//    }

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
                            if (binding.getValue() instanceof KiWiNode) {
                                map.put(binding.getName(), binding.getValue());
                            } else {
                                log.error("binding value {} is not a KiWiNode!", binding.getValue());
                            }
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
