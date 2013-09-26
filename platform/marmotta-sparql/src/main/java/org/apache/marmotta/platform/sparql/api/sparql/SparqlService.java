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
package org.apache.marmotta.platform.sparql.api.sparql;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.services.sparqlio.rdf.SPARQLGraphResultWriter;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.QueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.repository.RepositoryException;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public interface SparqlService {
	
	/**
	 * Parse query 
	 * 
	 * @param language language 
	 * @param query query
	 * @return query parsed
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	Query parseQuery(QueryLanguage language, String query) throws RepositoryException, MalformedQueryException ;
	
	/**
	 * Parse and return the concrete query type
	 * 
	 * @param language
	 * @param query
	 * @return
	 */
	QueryType getQueryType(QueryLanguage language, String query) throws MalformedQueryException;

	/**
	 * Evaluate a SPARQL query on the KiWi TripleStore. Writes the query results 
     * to the stream passed in the format requested.
     * 
	 *
     * @param query query
     * @param output strem to write
     * @param format mimetype
     * @param timeoutInSeconds
     * @throws MarmottaException
	 */
	void query(QueryLanguage language, String query, OutputStream output, String format, int timeoutInSeconds) throws MarmottaException, TimeoutException, MalformedQueryException;
	
    /**
     * Evaluate a SPARQL ASK query on the KiWi TripleStore
     *
     * see http://www.w3.org/TR/sparql11-query/
     *
     *
     * @param queryLanguage the query language to use
     * @param query         the SPARQL query to evaluate in SPARQL 1.1 syntax
     * @throws org.apache.marmotta.platform.core.exception.MarmottaException if the query evaluation fails
     */
	boolean ask(QueryLanguage queryLanguage, String query) throws MarmottaException;

    /**
     * Evaluate a SPARQL query on the KiWi TripleStore. Returns the results as a list of result maps, each element
     * a KiWiNode.
     *
     * see http://www.w3.org/TR/sparql11-query/
     *
     * @param queryLanguage the query language to use
     * @param query         the SPARQL query to evaluate in SPARQL 1.1 syntax
     */
    public List<Map<String,Value>> query(QueryLanguage queryLanguage, String query) throws MarmottaException;

    /**
     * Execute a SPARQL update on the KiWi TripleStore. Throws a KiWiException in case the update execution fails.
     *
     * see http://www.w3.org/TR/sparql11-update/
     *
     * @param queryLanguage
     * @param query  a string representing the update query in SPARQL Update 1.1 syntax
     * @throws Exception
     */
    void update(QueryLanguage queryLanguage, String query) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException;

    /**
     * Evaluate a SPARQL query, writing the results on the required writer.
     * 
     * @param queryLanguage
     * @param query
     * @param tupleWriter
     * @param booleanWriter
     * @param graphWriter
     * @param timeoutInSeconds
     * @throws MarmottaException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws TimeoutException
     */
    @Deprecated
    void query(QueryLanguage queryLanguage, String query, TupleQueryResultWriter tupleWriter, BooleanQueryResultWriter booleanWriter, SPARQLGraphResultWriter graphWriter, int timeoutInSeconds) throws MarmottaException, MalformedQueryException, QueryEvaluationException, TimeoutException;
    
    /**
     * Evaluate a SPARQL query, writing the results on the writer.
     * 
     * @param queryLanguage
     * @param query
     * @param writer
     * @param timeoutInSeconds
     */
    void query(QueryLanguage queryLanguage, String query, QueryResultWriter writer, int timeoutInSeconds) throws MarmottaException, MalformedQueryException, QueryEvaluationException, TimeoutException;
    
}
