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
package org.apache.marmotta.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.marmotta.client.exception.ParseException;
import org.apache.marmotta.client.model.meta.Metadata;
import org.apache.marmotta.client.model.rdf.BNode;
import org.apache.marmotta.client.model.rdf.Literal;
import org.apache.marmotta.client.model.rdf.RDFNode;
import org.apache.marmotta.client.model.rdf.URI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

/**
 * Parse RDF/JSON into a map-based representation.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class RDFJSONParser {

    public static final String NULL = "null";

    public static final String GRAPHS = "graphs";

    public static final String URI = "uri";

    public static final String BNODE = "bnode";

    public static final String DATATYPE = "datatype";

    public static final String LITERAL = "literal";

    public static final String LANG = "lang";

    public static final String TYPE = "type";

    public static final String VALUE = "value";

    private RDFJSONParser() {
        // static only
    }

    public static Map<String, Metadata> parseRDFJSON(InputStream data) throws ParseException {
        try {
            Model model = Rio.parse(data, "", RDFFormat.RDFJSON);

            // convert Sesame Model into a map to Metadata objects
            Map<String, Metadata> result = new HashMap<String, Metadata>();

            for (Resource subject : model.subjects()) {
                Metadata m = new Metadata(subject.stringValue());
                for (org.eclipse.rdf4j.model.IRI property : model.filter(subject, null, null).predicates()) {
                    Set<RDFNode> propValue = new HashSet<RDFNode>();
                    for (Value value : model.filter(subject, property, null).objects()) {
                        propValue.add(parseRDFJSONNode(value));
                    }
                    m.put(property.stringValue(), propValue);
                }
                result.put(subject.stringValue(), m);
            }
            return result;
        } catch (IOException e) {
            throw new ParseException("could not parse JSON data", e);
        } catch (RDFParseException e) {
            throw new ParseException("could not parse JSON data", e);
        } catch (UnsupportedRDFormatException e) {
            throw new ParseException("could not parse JSON data", e);
        }

    }

    /**
     * Parse the representation of a node in RDF/JSON into an RDFNode object
     *
     * @param nodeDef
     * @return
     */
    public static RDFNode parseRDFJSONNode(Map<String, String> nodeDef) {
        RDFNode object;

        if (nodeDef.get(TYPE).equals(URI)) {
            object = new URI(nodeDef.get(VALUE));
        } else if (nodeDef.get(TYPE).equals(BNODE)) {
            object = new BNode(nodeDef.get(VALUE));
        } else {
            if (nodeDef.get(LANG) != null) {
                object = new Literal(nodeDef.get(VALUE), nodeDef.get(LANG));
            } else if (nodeDef.get(DATATYPE) != null) {
                object = new Literal(nodeDef.get(VALUE), new URI(nodeDef.get(DATATYPE)));
            } else {
                object = new Literal(nodeDef.get(VALUE));
            }
        }
        return object;
    }

    /**
     * Parse the representation of a node in RDF/JSON into an RDFNode object
     *
     * @param nodeDef
     * @return
     */
    public static RDFNode parseRDFJSONNode(Value value) {
        RDFNode object;

        if (value instanceof org.eclipse.rdf4j.model.IRI) {
            object = new URI(value.stringValue());
        } else if (value instanceof BNode) {
            object = new BNode(value.stringValue());
        } else {
            org.eclipse.rdf4j.model.Literal literal = (org.eclipse.rdf4j.model.Literal) value;
            if (literal.getLanguage().orElse(null) != null) {
                object = new Literal(literal.getLabel(), literal.getLanguage().orElse(null));
            } else if (literal.getDatatype() != null) {
                object = new Literal(literal.getLabel(), new URI(literal.getDatatype().stringValue()));
            } else {
                object = new Literal(literal.getLabel());
            }
        }
        return object;
    }

    public static void serializeRDFJSON(Map<String, Metadata> data, OutputStream out) throws IOException {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model results = new LinkedHashModel();

        for (Map.Entry<String, Metadata> subject : data.entrySet()) {
            Resource subjectResource = stringToResource(subject.getKey(), vf);
            for (Map.Entry<String, Set<RDFNode>> predicate : subject.getValue().entrySet()) {
                org.eclipse.rdf4j.model.IRI predicateURI = vf.createIRI(predicate.getKey());
                for (RDFNode objectNode : predicate.getValue()) {
                    org.eclipse.rdf4j.model.Value objectValue;
                    if (objectNode instanceof Literal) {
                        if (((Literal) objectNode).getLanguage() != null) {
                            objectValue = vf.createLiteral(((Literal) objectNode).getContent(),
                                    ((Literal) objectNode).getLanguage());
                        } else if (((Literal) objectNode).getType() != null) {
                            objectValue = vf.createLiteral(((Literal) objectNode).getContent(),
                                    vf.createIRI(((Literal) objectNode).getType().getUri()));
                        } else {
                            objectValue = vf.createLiteral(((Literal) objectNode).getContent());
                        }
                    } else {
                        if (objectNode instanceof URI) {
                            objectValue = vf.createIRI(((URI) objectNode).getUri());
                        } else {
                            objectValue = vf.createBNode(((BNode) objectNode).getAnonId());
                        }
                    }
                    results.add(subjectResource, predicateURI, objectValue);
                }
            }

        }

        try {
            Rio.write(results, out, RDFFormat.RDFJSON);
        } catch (RDFHandlerException e) {
            throw new IOException(e);
        }
    }

    private static org.eclipse.rdf4j.model.Resource stringToResource(String resource, ValueFactory vf) {
        if (resource.startsWith("_:")) {
            return vf.createBNode(resource.substring(2));
        } else {
            return vf.createIRI(resource);
        }
    }
}
