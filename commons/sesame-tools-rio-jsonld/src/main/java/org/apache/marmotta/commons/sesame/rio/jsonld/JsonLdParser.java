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
package org.apache.marmotta.commons.sesame.rio.jsonld;

import com.google.common.base.Preconditions;
import de.dfki.km.json.jsonld.JSONLDProcessor;
import de.dfki.km.json.jsonld.JSONLDTripleCallback;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class JsonLdParser extends RDFParserBase implements JSONLDTripleCallback {

    private static Logger log = LoggerFactory.getLogger(JsonLdParser.class);

    JSONLDProcessor processor;

    /**
     * Creates a new RDFParserBase that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create RDF model objects.
     */
    public JsonLdParser() {
        processor =  new JSONLDProcessor();
    }

    /**
     * Creates a new RDFParserBase that will use the supplied ValueFactory to
     * create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    public JsonLdParser(ValueFactory valueFactory) {
        super(valueFactory);
        processor = new JSONLDProcessor();
    }

    /**
     * Gets the RDF format that this parser can parse.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.JSONLD;
    }

    /**
     * Parses the data from the supplied InputStream, using the supplied baseURI
     * to resolve any relative URI references.
     *
     * @param in      The InputStream from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                             If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                             If the configured statement handler has encountered an
     *                             unrecoverable error.
     */
    @Override
    public void parse(InputStream in, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        if(baseURI != null)
            setBaseURI(baseURI);

        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> object = mapper.readValue(in,new TypeReference<Map<String,Object>>() {});

        try {
            processor.triples(object,this);
        } catch(IllegalArgumentException ex) {
            if(ex.getCause() instanceof RDFParseException)
                throw (RDFParseException)ex.getCause();
            else {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    /**
     * Parses the data from the supplied Reader, using the supplied baseURI to
     * resolve any relative URI references.
     *
     * @param reader  The Reader from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                             If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                             If the configured statement handler has encountered an
     *                             unrecoverable error.
     */
    @Override
    public void parse(Reader reader, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        if(baseURI != null)
            setBaseURI(baseURI);

        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> object = mapper.readValue(reader,new TypeReference<Map<String,Object>>() {});

        try {
            processor.triples(object,this);
        } catch(IllegalArgumentException ex) {
            if(ex.getCause() instanceof RDFParseException)
                throw (RDFParseException)ex.getCause();
            else {
                throw new IllegalArgumentException(ex);
            }
        }
    }


    /**
     * Construct a triple with three URIs.
     *
     * @param s The Subject URI
     * @param p The Predicate URI
     * @param o The Object URI
     * @return The generated triple, or null to force triple generation to stop
     */
    @Override
    public void triple(String s, String p, String o) {
        // This method is always called with three URIs as subject predicate and
        // object
        try {
            rdfHandler.handleStatement(createStatement(resolveURI(s), resolveURI(p), resolveURI(o)));
        } catch (OpenRDFException e) {
            log.error("RDF Parse Error while creating statement",e);
            throw new IllegalArgumentException("RDF Parse Error while creating statement",e);
        }
    }

    /**
     * Constructs a triple with a Literal object, which may or may not contain a
     * language and/or a datatype.
     *
     * @param s        The Subject URI
     * @param p        The Predicate URI
     * @param value    The literal value
     * @param datatype The literal datatype
     * @param language The literal language (NOTE: may be null if not specified!)
     * @return The generated triple, or null to force triple generation to stop
     */
    @Override
    public void triple(String s, String p, String value, String datatype, String language) {
        Preconditions.checkNotNull(s);
        Preconditions.checkNotNull(p);
        Preconditions.checkNotNull(value);

        try {
            URI subject = createURI(s);

            URI predicate = createURI(p);

            Value object;
            if (language != null) {
                object = createLiteral(value, language, null);
            } else if (datatype != null) {
                object = createLiteral(value, null, createURI(datatype));
            } else {
                object = createLiteral(value, null, null);
            }

            rdfHandler.handleStatement(createStatement(subject, predicate, object));

        } catch (OpenRDFException e) {
            log.error("RDF Parse Error while creating statement",e);
            throw new IllegalArgumentException("RDF Parse Error while creating statement",e);
        }
    }
}
