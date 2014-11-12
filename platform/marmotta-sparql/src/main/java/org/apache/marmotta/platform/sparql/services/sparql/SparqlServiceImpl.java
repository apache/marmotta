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

import info.aduna.lang.FileFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.SPARQL_SD;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.sparql.api.sparql.QueryType;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.platform.sparql.services.sparqlio.rdf.SPARQLGraphResultWriter;
import org.apache.marmotta.platform.sparql.services.sparqlio.sparqlhtml.SPARQLHTMLSettings;
import org.apache.marmotta.platform.sparql.webservices.SparqlWebService;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.*;
import org.openrdf.query.parser.*;
import org.openrdf.query.resultio.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.*;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Sparql Service implementation
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
@ApplicationScoped
public class SparqlServiceImpl implements SparqlService {

    /**
     * @deprecated beginning with Sesame 2.8, use {@link RDFFormat#getStandardURI()} or {@link QueryResultFormat#etStandardURI()}
     */
    @Deprecated
    private static final Map<FileFormat, String> w3cFormatID = new HashMap<FileFormat, String>() {
        private static final long serialVersionUID = 1L;
        {
        put(RDFFormat.JSONLD, "http://www.w3.org/ns/formats/JSON-LD");
        put(RDFFormat.N3, "http://www.w3.org/ns/formats/N3");
        put(RDFFormat.NTRIPLES, "http://www.w3.org/ns/formats/N-Triples");
        put(RDFFormat.NQUADS, "http://www.w3.org/ns/formats/N-Quads");
        put(RDFFormat.RDFA, "http://www.w3.org/ns/formats/RDFa");
        put(RDFFormat.RDFJSON, "http://www.w3.org/ns/formats/RDF_JSON");
        put(RDFFormat.RDFXML, "http://www.w3.org/ns/formats/RDF_XML");
        put(RDFFormat.TURTLE, "http://www.w3.org/ns/formats/Turtle");
        put(RDFFormat.TRIG, "http://www.w3.org/ns/formats/TriG");

        put(TupleQueryResultFormat.CSV, "http://www.w3.org/ns/formats/SPARQL_Results_CSV");
        put(TupleQueryResultFormat.JSON, "http://www.w3.org/ns/formats/SPARQL_Results_JSON");
        put(TupleQueryResultFormat.TSV, "http://www.w3.org/ns/formats/SPARQL_Results_TSV");
        put(TupleQueryResultFormat.SPARQL, "http://www.w3.org/ns/formats/SPARQL_Results_XML");
    }};


    /**
     * Get the seam logger for issuing logging statements.
     */
    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private TemplatingService templatingService;

    @Inject
    private SesameService sesameService;

    private ExecutorService executorService;

    private long queryId = 0;

    @PostConstruct
    public void initialize() {
        executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "SPARQL Query Thread " + (++queryId));
            }
        });
    }

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
    public QueryType getQueryType(QueryLanguage language, String query) throws MalformedQueryException {
        QueryParser parser = QueryParserUtil.createParser(language); 
        ParsedQuery parsedQuery = parser.parseQuery(query, configurationService.getServerUri() + SparqlWebService.PATH + "/" + SparqlWebService.SELECT);
        if (parsedQuery instanceof ParsedTupleQuery) {
            return QueryType.TUPLE;
        } else if (parsedQuery instanceof ParsedBooleanQuery) {
            return QueryType.BOOL;
        } else if (parsedQuery instanceof ParsedGraphQuery) {
            return QueryType.GRAPH;
        } else {
            return null;
        }
    }

    @Override
    @Deprecated
    public void query(final QueryLanguage queryLanguage, final String query, final TupleQueryResultWriter tupleWriter, final BooleanQueryResultWriter booleanWriter, final SPARQLGraphResultWriter graphWriter, int timeoutInSeconds) throws MarmottaException, MalformedQueryException, QueryEvaluationException, TimeoutException {

        log.debug("executing SPARQL query:\n{}", query);

        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                long start = System.currentTimeMillis();
                try {
                    RepositoryConnection connection = sesameService.getConnection();
                    try {
                        connection.begin();
                        Query sparqlQuery = connection.prepareQuery(queryLanguage, query, configurationService.getBaseUri());

                        if (sparqlQuery instanceof TupleQuery) {
                            query((TupleQuery) sparqlQuery, tupleWriter);
                        } else if (sparqlQuery instanceof BooleanQuery) {
                            query((BooleanQuery) sparqlQuery, booleanWriter);
                        } else if (sparqlQuery instanceof GraphQuery) {
                            query((GraphQuery) sparqlQuery, graphWriter.getOutputStream(), graphWriter.getFormat());
                        } else {
                            connection.rollback();
                            throw new InvalidArgumentException("SPARQL query type " + sparqlQuery.getClass() + " not supported!");
                        }

                        connection.commit();
                    } catch (Exception ex) {
                        connection.rollback();
                        throw ex;
                    } finally {
                        connection.close();
                    }
                } catch(RepositoryException e) {
                    log.error("error while getting repository connection: {}", e);
                    throw new MarmottaException("error while getting repository connection", e);
                } catch (QueryEvaluationException e) {
                    log.error("error while evaluating query: {}", e.getMessage());
                    throw new MarmottaException("error while writing query result in format ", e);
                }

                log.debug("SPARQL execution took {}ms", System.currentTimeMillis()-start);

                return Boolean.TRUE;
            }
        });

        try {
            future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.info("SPARQL query execution aborted due to timeout");
            future.cancel(true);
            throw new TimeoutException("SPARQL query execution aborted due to timeout (" + configurationService.getIntConfiguration("sparql.timeout",60)+"s)");
        } catch (ExecutionException e) {
            log.info("SPARQL query execution aborted due to exception");
            log.debug("exception details",e);
            if(e.getCause() instanceof MarmottaException) {
                throw (MarmottaException)e.getCause();
            } else if(e.getCause() instanceof MalformedQueryException) {
                throw (MalformedQueryException)e.getCause();
            } else {
                throw new MarmottaException("unknown exception while evaluating SPARQL query",e.getCause());
            }
        }
    }

    @Override
    @Deprecated
    public void query(final QueryLanguage queryLanguage, final String query, final QueryResultWriter writer, final int timeoutInSeconds) throws MarmottaException, MalformedQueryException, QueryEvaluationException, TimeoutException {
        log.debug("executing SPARQL query:\n{}", query);
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                long start = System.currentTimeMillis();
                try {
                    RepositoryConnection connection = sesameService.getConnection();
                    try {
                        connection.begin();
                        Query sparqlQuery = connection.prepareQuery(queryLanguage, query, configurationService.getBaseUri());

                        if (sparqlQuery instanceof TupleQuery) {
                            query((TupleQuery) sparqlQuery, (TupleQueryResultWriter)writer);
                        } else if (sparqlQuery instanceof BooleanQuery) {
                            query((BooleanQuery) sparqlQuery, (BooleanQueryResultWriter)writer);
                        } else if (sparqlQuery instanceof GraphQuery) {
                            query((GraphQuery) sparqlQuery, ((SPARQLGraphResultWriter)writer).getOutputStream(), ((SPARQLGraphResultWriter)writer).getFormat());
                        } else {
                            connection.rollback();
                            throw new InvalidArgumentException("SPARQL query type " + sparqlQuery.getClass() + " not supported!");
                        }

                        connection.commit();
                    } catch (Exception ex) {
                        connection.rollback();
                        throw ex;
                    } finally {
                        connection.close();
                    }
                } catch(RepositoryException e) {
                    log.error("error while getting repository connection: {}", e);
                    throw new MarmottaException("error while getting repository connection", e);
                } catch (QueryEvaluationException e) {
                    log.error("error while evaluating query: {}", e.getMessage());
                    throw new MarmottaException("error while writing query result in format ", e);
                }

                log.debug("SPARQL execution took {}ms", System.currentTimeMillis()-start);

                return Boolean.TRUE;
            }
        });

        try {
            future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.info("SPARQL query execution aborted due to timeout");
            future.cancel(true);
            throw new TimeoutException("SPARQL query execution aborted due to timeout (" + timeoutInSeconds+"s)");
        } catch (ExecutionException e) {
            log.info("SPARQL query execution aborted due to exception");
            log.debug("exception details", e);
            if(e.getCause() instanceof MarmottaException) {
                throw (MarmottaException)e.getCause();
            } else if(e.getCause() instanceof MalformedQueryException) {
                throw (MalformedQueryException)e.getCause();
            } else {
                throw new MarmottaException("unknown exception while evaluating SPARQL query",e.getCause());
            }
        }    	
    }

    @Override
    public void query(final QueryLanguage language, final String query, final OutputStream output, final String format, int timeoutInSeconds) throws MarmottaException, TimeoutException, MalformedQueryException {
        log.debug("executing SPARQL query:\n{}", query);
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                long start = System.currentTimeMillis();
                try {
                    RepositoryConnection connection = sesameService.getConnection();
                    try {
                        connection.begin();
                        Query sparqlQuery = connection.prepareQuery(language, query, configurationService.getBaseUri());

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
                    } catch (Exception ex) {
                        connection.rollback();
                        throw ex;
                    } finally {
                        connection.close();
                    }
                } catch(RepositoryException e) {
                    log.error("error while getting repository connection: {}", e);
                    throw new MarmottaException("error while getting repository connection", e);
                } catch (QueryEvaluationException e) {
                    log.error("error while evaluating query: {}", e);
                    throw new MarmottaException("error while evaluating query ", e);
                } catch (MalformedQueryException e) {
                    log.error("error because malformed query: {}", e);
                    throw new MarmottaException("error because malformed query", e);
                }

                log.debug("SPARQL execution took {}ms", System.currentTimeMillis()-start);
                return Boolean.TRUE;
            }
        });

        try {
            future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.info("SPARQL query execution aborted due to timeout");
            future.cancel(true);
            throw new TimeoutException("SPARQL query execution aborted due to timeout (" + configurationService.getIntConfiguration("sparql.timeout",60)+"s)");
        } catch (ExecutionException e) {
            log.info("SPARQL query execution aborted due to exception");
            log.debug("exception details", e);
            if(e.getCause() instanceof MarmottaException) {
                throw (MarmottaException)e.getCause();
            } else if(e.getCause() instanceof MalformedQueryException) {
                throw (MalformedQueryException)e.getCause();
            } else {
                throw new MarmottaException("unknown exception while evaluating SPARQL query",e.getCause());
            }
        }

    }

    private void query(TupleQuery query, TupleQueryResultWriter writer) throws QueryEvaluationException {
        try {
            query.evaluate(writer);
        } catch (TupleQueryResultHandlerException e) {
            throw new QueryEvaluationException("error while writing query tuple result: ",e);
        }
    }

    private void query(TupleQuery query, OutputStream output, String format) throws QueryEvaluationException {
        query(query, getTupleResultWriter(format, output));
    }

    private void query(BooleanQuery query, BooleanQueryResultWriter writer) throws QueryEvaluationException {
        try {
            writer.handleBoolean(query.evaluate());
        } catch (QueryResultHandlerException e) {
            throw new QueryEvaluationException("error while writing query boolean result: ",e);
        }
    }

    private void query(BooleanQuery query, OutputStream output, String format) throws QueryEvaluationException {
        query(query, getBooleanResultWriter(format, output));
    }

    private void query(GraphQuery query, OutputStream output, String format) throws QueryEvaluationException {
        query(query, output, Rio.getWriterFormatForMIMEType(format, RDFFormat.RDFXML));
    }

    private void query(GraphQuery query, OutputStream output, RDFFormat format) throws QueryEvaluationException {
        try {
            QueryResultIO.write(query.evaluate(), format, output);
        } catch (IOException e) {
            throw new QueryEvaluationException("error while writing query graph result: ",e);
        }
        catch(RDFHandlerException e) {
            throw new QueryEvaluationException("error while writing query graph result: ",e);
        }
        catch(UnsupportedRDFormatException e) {
            throw new QueryEvaluationException("Could not find requested output RDF format for results of query: ",e);
        }
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
                BooleanQuery ask = connection.prepareBooleanQuery(queryLanguage, query, configurationService.getBaseUri());
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

    @Override
    public void createServiceDescription(RDFWriter writer, String requestURL, boolean isUpdate) throws RDFHandlerException {
        try {
            writer.startRDF();
            final ValueFactory vf = new ValueFactoryImpl();
            writer.handleNamespace(SPARQL_SD.PREFIX, SPARQL_SD.NAMESPACE);
            writer.handleNamespace("formats", "http://www.w3.org/ns/formats/");
            writer.handleNamespace("void", "http://rdfs.org/ns/void#");

            final BNode sd = vf.createBNode();
            writer.handleStatement(vf.createStatement(sd, RDF.TYPE, SPARQL_SD.Service));
            writer.handleStatement(vf.createStatement(sd, SPARQL_SD.endpoint, vf.createURI(requestURL)));
            writer.handleStatement(vf.createStatement(sd, SPARQL_SD.supportedLanguage, isUpdate?SPARQL_SD.SPARQL11Update:SPARQL_SD.SPARQL11Query));

            if (!isUpdate) {
                // FIXME: really? these types?
                final Set<FileFormat> formats = new HashSet<>();
                formats.addAll(RDFWriterRegistry.getInstance().getKeys());
                formats.addAll(TupleQueryResultFormat.values());
                for (FileFormat f: formats) {
                    final String formatUri = w3cFormatID.get(f);
                    if (StringUtils.isNotBlank(formatUri)) {
                        writer.handleStatement(vf.createStatement(sd, SPARQL_SD.resultFormat, vf.createURI(formatUri)));
                    } else {
                        final BNode fNode = vf.createBNode();
                        writer.handleStatement(vf.createStatement(sd, SPARQL_SD.resultFormat, fNode));
                        writer.handleStatement(vf.createStatement(fNode, RDF.TYPE, vf.createURI("http://www.w3.org/ns/formats/Format")));
                        writer.handleStatement(vf.createStatement(fNode, vf.createURI("http://www.w3.org/ns/formats/media_type"), vf.createLiteral(f.getDefaultMIMEType())));
                        writer.handleStatement(vf.createStatement(fNode, vf.createURI("http://www.w3.org/ns/formats/preferred_suffix"), vf.createLiteral("."+f.getDefaultFileExtension())));
                    }
                }
            }

            final BNode dataset = vf.createBNode();
            writer.handleStatement(vf.createStatement(sd, SPARQL_SD.defaultDataset, dataset));
            writer.handleStatement(vf.createStatement(dataset, RDF.TYPE, SPARQL_SD.Dataset));

            final RepositoryConnection kiwiCon = sesameService.getConnection();
            try {
                kiwiCon.begin();
                // FIXME: Default graph, in KiWi this is all - is it not?
                final BNode defaultGraph = vf.createBNode();
                writer.handleStatement(vf.createStatement(dataset, SPARQL_SD.defaultGraph, defaultGraph));
                writer.handleStatement(vf.createStatement(defaultGraph, RDF.TYPE, SPARQL_SD.Graph));
                // TODO: Number of triples here? This can be expensive!
                writer.handleStatement(vf.createStatement(defaultGraph, vf.createURI("http://rdfs.org/ns/void#triples"), vf.createLiteral(kiwiCon.size())));

                final RepositoryResult<Resource> cID = kiwiCon.getContextIDs();
                try {
                    while (cID.hasNext()) {
                        final Resource c = cID.next();
                        if (c instanceof URI) {
                            // A named graph
                            final BNode ng = vf.createBNode();
                            writer.handleStatement(vf.createStatement(dataset, SPARQL_SD.namedGraph, ng));
                            writer.handleStatement(vf.createStatement(ng, RDF.TYPE, SPARQL_SD.NamedGraph));
                            writer.handleStatement(vf.createStatement(ng, SPARQL_SD.name, c));
                            final BNode g = vf.createBNode();
                            writer.handleStatement(vf.createStatement(ng, SPARQL_SD.graph, g));
                            writer.handleStatement(vf.createStatement(g, RDF.TYPE, SPARQL_SD.Graph));
                            // TODO: Number of triples here? This can be expensive!
                            writer.handleStatement(vf.createStatement(g, vf.createURI("http://rdfs.org/ns/void#triples"), vf.createLiteral(kiwiCon.size(c))));

                        }
                    }
                } finally {
                    cID.close();
                }

                kiwiCon.commit();
            } catch (final Throwable t){
                kiwiCon.rollback();
                throw t;
            } finally {
                kiwiCon.close();
            } 

            writer.endRDF();
        } catch (RepositoryException e) {
            throw new RDFHandlerException("Could not build SparqlServiceDescription");
        }
    }

    private TupleQueryResultWriter getTupleResultWriter(String format, OutputStream os) {
        TupleQueryResultFormat resultFormat;
        if(format == null) {
            resultFormat = TupleQueryResultFormat.SPARQL;
        } else {
            resultFormat = QueryResultIO.getWriterFormatForMIMEType(format);
            if(resultFormat == null) {
                throw new InvalidArgumentException("could not produce format "+format);
            }
        } 
        TupleQueryResultWriter writer = QueryResultIO.createWriter(resultFormat, os);
        if(writer.getSupportedSettings().contains(SPARQLHTMLSettings.TEMPLATING_SERVICE)) {
            writer.getWriterConfig().set(SPARQLHTMLSettings.TEMPLATING_SERVICE, templatingService);
        }
        return writer;
    }

    private BooleanQueryResultWriter getBooleanResultWriter(String format, OutputStream os) {
        BooleanQueryResultFormat resultFormat;
        if(format == null) {
            resultFormat = BooleanQueryResultFormat.SPARQL;
        } else {
            resultFormat = QueryResultIO.getBooleanWriterFormatForMIMEType(format);
            if(resultFormat == null) {
                throw new InvalidArgumentException("could not produce format "+format);
            }
        } 
        return QueryResultIO.createWriter(resultFormat, os);
    }
}
