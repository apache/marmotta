/*
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
package org.apache.marmotta.loader.titan;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import org.openrdf.model.*;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.util.HashSet;
import java.util.Set;

/**
 * An RDF handler for bulk loading RDF data into a Blueprints graph accessed through a Blueprints
 * GraphSail.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class TitanRDFHandler  implements RDFHandler {

    public static final String SEPARATOR = " ";

    public static final String
            PREDICATE_PROP = "p",
            CONTEXT_PROP = "c";

    public static final char
            URI_PREFIX = 'U',
            BLANK_NODE_PREFIX = 'B',
            PLAIN_LITERAL_PREFIX = 'P',
            TYPED_LITERAL_PREFIX = 'T',
            LANGUAGE_TAG_LITERAL_PREFIX = 'L',
            NULL_CONTEXT_PREFIX = 'N';

    public static final String
            BNODE = "bnode",
            INFERRED = "inferred",
            KIND = "kind",
            LANG = "lang",
            LITERAL = "literal",
            TYPE = "type",
            URI = "uri",
            VALUE = "value";


    public static final String NULL_CONTEXT_NATIVE = "" + NULL_CONTEXT_PREFIX;

    private static final String NAMESPACES_VERTEX_ID = "urn:com.tinkerpop.blueprints.pgm.oupls.sail:namespaces";

    public static final String DEFAULT_NAMESPACE_PREFIX_KEY = "default-namespace";

    private BatchGraph<TitanGraph> graph;

    private Vertex namespaces;

    private long triples = 0;


    /**
     * Initialise the handler using the graph passed as arguments.
     *
     * @param graph
     */
    public TitanRDFHandler(TitanGraph graph, String indexes) {
        this.graph = new BatchGraph(graph);

        if (!graph.getIndexedKeys(Vertex.class).contains(VALUE)) {
            graph.createKeyIndex(VALUE, Vertex.class);
        }

        createTripleIndices(indexes, graph);

        namespaces = graph.addVertex(NAMESPACES_VERTEX_ID);

    }

    public TitanRDFHandler() {
        super();
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {

    }

    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        if(graph instanceof TransactionalGraph) {
            ((TransactionalGraph)graph).commit();
        }
    }

    /**
     * Handles a namespace declaration/definition. A namespace declaration
     * associates a (short) prefix string with the namespace's URI. The prefix
     * for default namespaces, which do not have an associated prefix, are
     * represented as empty strings.
     *
     * @param prefix The prefix for the namespace, or an empty string in case of a
     *               default namespace.
     * @param uri    The URI that the prefix maps to.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        namespaces.setProperty(toNativePrefixKey(prefix), uri);
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {

        Vertex out = getOrCreateVertex(st.getSubject());
        Vertex in = getOrCreateVertex(st.getObject());
        Edge edge = graph.addEdge(null, out, in, st.getPredicate().stringValue());

        if (null == edge.getProperty(CONTEXT_PROP)) {
            edge.setProperty(CONTEXT_PROP, valueToNative(st.getContext()));
        }

        triples ++;

        if(triples % 10000 == 0 && graph instanceof TransactionalGraph) {
            ((TransactionalGraph)graph).commit();
        }
    }

    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {

    }


    private Vertex addVertex(final Value value) {
        Vertex v = graph.addVertex(valueToNative(value));

        if (value instanceof URI) {
            v.setProperty(KIND, URI);
            v.setProperty(VALUE, value.stringValue());
        } else if (value instanceof Literal) {
            Literal l = (Literal) value;
            v.setProperty(KIND, LITERAL);
            v.setProperty(VALUE, l.getLabel());
            if (null != l.getDatatype()) {
                v.setProperty(TYPE, l.getDatatype().stringValue());
            }
            if (null != l.getLanguage()) {
                v.setProperty(LANG, l.getLanguage());
            }
        } else if (value instanceof BNode) {
            BNode b = (BNode) value;
            v.setProperty(KIND, BNODE);
            v.setProperty(VALUE, b.getID());
        } else {
            throw new IllegalStateException("value of unexpected type: " + value);
        }

        return v;
    }

    private Vertex findVertex(final Value value) {
        return graph.getVertex(valueToNative(value));
    }


    private Vertex getOrCreateVertex(final Value value) {
        Vertex v = findVertex(value);
        if (null == v) {
            v = addVertex(value);
        }
        return v;
    }


    public String valueToNative(final Value value) {
        if (null == value) {
            return NULL_CONTEXT_NATIVE;
        } else if (value instanceof Resource) {
            return resourceToNative((Resource) value);
        } else if (value instanceof Literal) {
            return literalToNative((Literal) value);
        } else {
            throw new IllegalStateException("Value has unfamiliar type: " + value);
        }
    }

    public String resourceToNative(final Resource value) {
        if (value instanceof URI) {
            return uriToNative((URI) value);
        } else if (value instanceof BNode) {
            return bnodeToNative((BNode) value);
        } else {
            throw new IllegalStateException("Resource has unfamiliar type: " + value);
        }
    }

    public String uriToNative(final URI value) {
        return URI_PREFIX + SEPARATOR + value.toString();
    }

    public String bnodeToNative(final BNode value) {
        return BLANK_NODE_PREFIX + SEPARATOR + value.getID();
    }

    public String literalToNative(final Literal literal) {
        URI datatype = literal.getDatatype();

        if (null == datatype) {
            String language = literal.getLanguage();

            if (null == language) {
                return PLAIN_LITERAL_PREFIX + SEPARATOR + literal.getLabel();
            } else {
                return LANGUAGE_TAG_LITERAL_PREFIX + SEPARATOR + language + SEPARATOR + literal.getLabel();
            }
        } else {
            return "" + TYPED_LITERAL_PREFIX + SEPARATOR + datatype + SEPARATOR + literal.getLabel();
        }
    }

    private String toNativePrefixKey(final String prefix) {
        return 0 == prefix.length() ? DEFAULT_NAMESPACE_PREFIX_KEY : prefix;
    }


    private void createTripleIndices(final String tripleIndexes, TitanGraph titanGraph) {
        if (null == tripleIndexes) {
            throw new IllegalArgumentException("index list, if supplied, must be non-null");
        }

        Set<String> u = new HashSet<String>();

        String[] a = tripleIndexes.split(",");
        for (String s : a) {
            String pattern = s.trim();
            if (pattern.length() > 0) {
                u.add(pattern);
            }
        }

        for (String key : u) {
            if (!titanGraph.getIndexedKeys(Edge.class).contains(key)) {
                titanGraph.createKeyIndex(key, Edge.class);
            }
        }
    }


}
