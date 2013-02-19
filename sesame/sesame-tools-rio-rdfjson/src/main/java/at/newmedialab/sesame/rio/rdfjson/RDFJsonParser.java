/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.sesame.rio.rdfjson;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class RDFJsonParser extends RDFParserBase {


    private static final String HTTP = "http://";
    private static final String VALUE = "value";
    private static final String TYPE = "type";
    private static final String BNODE = "bnode";
    private static final String URI = "uri";
    private static final String LANG = "lang";
    private static final String DATATYPE = "datatype";

    /**
     * Creates a new RDFParserBase that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create RDF model objects.
     */
    public RDFJsonParser() {
        super();
    }

    /**
     * Creates a new TurtleParser that will use the supplied ValueFactory to
     * create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    public RDFJsonParser(ValueFactory valueFactory) {
        super(valueFactory);
    }

    /**
     * Gets the RDF format that this parser can parse.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.RDFJSON;
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
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Map<String,Set<Map<String,String>>>> subjects = mapper.readValue(in, new TypeReference<Map<String,Map<String,Set<Map<String,String>>>>>(){});
        addToRepository(subjects);
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
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Map<String,Set<Map<String,String>>>> subjects = mapper.readValue(reader, new TypeReference<Map<String,Map<String,Set<Map<String,String>>>>>(){});
        addToRepository(subjects);
    }



    private void addToRepository(Map<String,Map<String,Set<Map<String,String>>>> subjects) throws RDFParseException, RDFHandlerException {
        for(String subjectKey : subjects.keySet() ) {
            //create subject Resource
            Resource subject;
            if(subjectKey.startsWith(HTTP)) {
                subject = new URIImpl(subjectKey);
            } else {
                subject = new BNodeImpl(subjectKey);
            }

            for(Map.Entry<String, Set<Map<String, String>>> entry : subjects.get(subjectKey).entrySet()) {
                //create property URI
                URIImpl property = new URIImpl(entry.getKey());

                for(Map<String,String> o : entry.getValue()) {
                    //create object resources
                    Value object;

                    if( o.get(TYPE).equals(URI) ) {
                        object = createURI(o.get(VALUE));
                    } else if( o.get(TYPE).equals(BNODE) ) {
                        object = createBNode(o.get(VALUE));
                    } else {
                        if( o.get(LANG) != null ) {
                            object = createLiteral(o.get(VALUE),o.get(LANG),null);
                        } else if( o.get(DATATYPE) != null) {
                            object = createLiteral(o.get(VALUE),null,new URIImpl(o.get(DATATYPE)));
                        } else {
                            object = createLiteral(o.get(VALUE),null,null);
                        }
                    }
                    //add triple
                    rdfHandler.handleStatement(createStatement(subject, property, object));
                }
            }
        }
    }

}
