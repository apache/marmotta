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
package org.apache.marmotta.client.util;

import org.apache.marmotta.client.exception.ParseException;
import org.apache.marmotta.client.model.meta.Metadata;
import org.apache.marmotta.client.model.rdf.BNode;
import org.apache.marmotta.client.model.rdf.Literal;
import org.apache.marmotta.client.model.rdf.RDFNode;
import org.apache.marmotta.client.model.rdf.URI;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parse RDF/JSON into a map-based representation.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class RDFJSONParser {

    @SuppressWarnings("unused")
	private static final String HTTP = "http://";
    private static final String VALUE = "value";
    private static final String TYPE = "type";
    private static final String TYPE_BNODE = "bnode";
    private static final String TYPE_URI = "uri";
    private static final String TYPE_LITERAL = "literal";
    private static final String LANG = "lang";
    private static final String DATATYPE = "datatype";

    private RDFJSONParser() {
		// static only
	}
    
    public static Map<String,Metadata> parseRDFJSON(InputStream data) throws ParseException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String,Map<String,Set<Map<String,String>>>> subjects = mapper.readValue(data, new TypeReference<Map<String,Map<String,Set<Map<String,String>>>>>(){});

            // convert "raw" map into a map to Metadata objects
            Map<String,Metadata> result = new HashMap<String, Metadata>();
            for(Map.Entry<String,Map<String,Set<Map<String,String>>>> subject : subjects.entrySet()) {
                Metadata m = new Metadata(subject.getKey());
                result.put(subject.getKey(),m);

                for(Map.Entry<String,Set<Map<String,String>>> property : subject.getValue().entrySet()) {
                    Set<RDFNode> propValue = new HashSet<RDFNode>();
                    for(Map<String,String> value : property.getValue()) {
                        propValue.add(parseRDFJSONNode(value));
                    }
                    m.put(property.getKey(),propValue);
                }
            }
            return result;

        } catch (IOException e) {
            throw new ParseException("could not parse JSON data",e);
        }

    }

    /**
     * Parse the representation of a node in RDF/JSON into an RDFNode object
     * @param nodeDef
     * @return
     */
    public static RDFNode parseRDFJSONNode(Map<String, String> nodeDef) {
        RDFNode object;

        if( nodeDef.get(TYPE).equals(TYPE_URI) ) {
            object = new URI(nodeDef.get(VALUE));
        } else if( nodeDef.get(TYPE).equals(TYPE_BNODE) ) {
            object = new BNode(nodeDef.get(VALUE));
        } else {
            if( nodeDef.get(LANG) != null ) {
                object = new Literal(nodeDef.get(VALUE),nodeDef.get(LANG));
            } else if( nodeDef.get(DATATYPE) != null) {
                object = new Literal(nodeDef.get(VALUE),new URI(nodeDef.get(DATATYPE)));
            } else {
                object = new Literal(nodeDef.get(VALUE));
            }
        }
        return object;
    }
    
   
    public static void serializeRDFJSON(Map<String,Metadata> data, OutputStream out) throws IOException {
        ObjectMapper mapper = new ObjectMapper();


        Map<String,Map<String,Set<Map<String,String>>>> subjects = new HashMap<String, Map<String, Set<Map<String, String>>>>();

        
        for(Map.Entry<String,Metadata> subject : data.entrySet()) {
            //add or get predicate map
            Map<String,Set<Map<String,String>>> predicates = new HashMap<String,Set<Map<String,String>>>();
            subjects.put(subject.getKey(),predicates);
            
 
            for(Map.Entry<String,Set<RDFNode>> predicate : subject.getValue().entrySet()) {
                //add or get object set
                Set<Map<String,String>> objects = new HashSet<Map<String,String>>();
                predicates.put(predicate.getKey(),objects);

                //add objects
                for(RDFNode objectNode : predicate.getValue()) {
                    Map<String,String> object = new HashMap<String,String>();
                    if( objectNode instanceof Literal) {
                        object.put(TYPE,TYPE_LITERAL);
                        object.put(VALUE,((Literal)objectNode).getContent());
                        if(((Literal) objectNode).getLanguage() != null )
                            object.put(LANG,((Literal) objectNode).getLanguage());
                        if(((Literal) objectNode).getType() != null)
                            object.put(DATATYPE,((Literal) objectNode).getType().getUri());
                    } else {
                        if( objectNode instanceof URI ) {
                            object.put(TYPE,TYPE_URI);
                            object.put(VALUE,((URI)objectNode).getUri());
                        } else {
                            object.put(TYPE,TYPE_BNODE);
                            object.put(VALUE,((BNode)objectNode).getAnonId());
                        }
                    }
                    objects.add(object);
                }
            }
                
        }
        mapper.writeValue(out,subjects);
                
    }
}
