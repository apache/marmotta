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
package org.apache.marmotta.commons.sesame.rio.rdfjson;

import org.codehaus.jackson.map.ObjectMapper;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class RDFJsonWriter implements RDFWriter {

    private static final String VALUE = "value";
    private static final String TYPE = "type";
    private static final String BNODE = "bnode";
    private static final String URI = "uri";
    private static final String LITERAL = "literal";
    private static final String LANG = "lang";
    private static final String DATATYPE = "datatype";


    private Writer writer;

    private HashMap<String,HashMap<String,HashSet<HashMap<String,String>>>> subjects;


    public RDFJsonWriter(OutputStream out) {
        writer = new OutputStreamWriter(out);
    }


    public RDFJsonWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Gets the RDF format that this RDFWriter uses.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.RDFJSON;
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        subjects = new HashMap<String,HashMap<String,HashSet<HashMap<String,String>>>>();
    }

    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(writer,subjects);
        } catch (IOException e) {
            throw new RDFHandlerException("error while serializing JSON objects",e);
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
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
            // get subject key
            String subjectKey;
            if( st.getSubject() instanceof URI) {
                subjectKey = ((URI)st.getSubject()).stringValue();
            } else {
                subjectKey = ((BNode)st.getSubject()).getID();
            }

            //add or get predicate map
            HashMap<String,HashSet<HashMap<String,String>>> predicates;
            if( subjects.containsKey(subjectKey)) {
                predicates = subjects.get(subjectKey);
            } else {
                predicates = new HashMap<String,HashSet<HashMap<String,String>>>();
                subjects.put(subjectKey,predicates);
            }

            //get predicate key
            String predicateKey = st.getPredicate().stringValue();

            //add or get object set
            HashSet<HashMap<String,String>> objects;
            if( predicates.containsKey(predicateKey) ) {
                objects = predicates.get(predicateKey);
            } else {
                 objects = new HashSet<HashMap<String,String>>();
                predicates.put(predicateKey,objects);
            }

            //add objects
            HashMap<String,String> object = new HashMap<String,String>();
            Value objectNode = st.getObject();
            if( objectNode instanceof Literal) {
                object.put(TYPE,LITERAL);
                object.put(VALUE,((Literal)objectNode).stringValue());
                if(((Literal) objectNode).getLanguage() != null )
                    object.put(LANG,((Literal) objectNode).getLanguage());
                if(((Literal) objectNode).getDatatype() != null)
                    object.put(DATATYPE,((Literal) objectNode).getDatatype().stringValue());
            } else {
                if( objectNode instanceof URI ) {
                    object.put(TYPE,URI);
                    object.put(VALUE,((URI)objectNode).stringValue());
                } else {
                    object.put(TYPE,BNODE);
                    object.put(VALUE,((BNode)objectNode).getID());
                }
            }
            objects.add(object);
    }

    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {
    }



}
