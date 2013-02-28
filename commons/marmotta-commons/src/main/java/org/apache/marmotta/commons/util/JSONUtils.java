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
package org.apache.marmotta.commons.util;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for transforming Sesame objects into in-memory RDF/JSON format using maps and sets.
 */
public class JSONUtils {

    private static final String VALUE = "value";
    private static final String TYPE = "type";
    private static final String BNODE = "bnode";
    private static final String URI = "uri";
    private static final String LITERAL = "literal";
    private static final String LANG = "lang";
    private static final String DATATYPE = "datatype";

    /**
     * Transform node into an in-memory RDF/JSON representation that can be serialised using e.g. the
     * Jackson ObjectMapper.
     *
     * @param node
     * @return
     */
    public static Map<String, String> serializeNodeAsJson(Value node) {
        Map<String,String> nodeRep = new HashMap<String, String>();
        if(node instanceof Literal) {
            Literal literal = (Literal)node;
            nodeRep.put(TYPE,LITERAL);
            nodeRep.put(VALUE, literal.stringValue());
            if(literal.getDatatype() != null) {
                nodeRep.put(DATATYPE,literal.getDatatype().stringValue());
            }
            if(literal.getLanguage() != null) {
                nodeRep.put(LANG,literal.getLanguage());
            }
        } else if(node instanceof URI) {
            nodeRep.put(TYPE,URI);
            nodeRep.put(VALUE,node.stringValue());
        } else if(node instanceof BNode) {
            nodeRep.put(TYPE,BNODE);
            nodeRep.put(VALUE,node.stringValue());
        }
        return nodeRep;
    
    }

    /**
     * Turn a list of nodes into a dataformat serializable by Jackson (maps and sets)
     *
     *
     * @param nodes
     * @return
     */
    public static Set<Map<String,String>> serializeNodesAsJson(Iterable<? extends Value> nodes) {
        Set<Map<String,String>> result = new HashSet<Map<String, String>>();

        for(Value objectNode : nodes) {
            result.add(serializeNodeAsJson(objectNode));
        }
        return result;
    }

    /**
     * This method wraps triples (Subject,Property,Object) in a dataformat that is serializable
     * by Jackson (namely maps and sets).
     *
     * @param triple
     * @return
     */
    public static Map<String,?> serializeTripleAsJson(Statement triple) {

        Map<String,Map<String,Set<Map<String,String>>>> subjects = new HashMap<String,Map<String,Set<Map<String,String>>>>();

        // get subject key
        String subjectKey = triple.getSubject().stringValue();

        //add or get predicate map
        Map<String,Set<Map<String,String>>> predicates;
        if( subjects.containsKey(subjectKey)) {
            predicates = subjects.get(subjectKey);
        } else {
            predicates = new HashMap<String,Set<Map<String,String>>>();
            subjects.put(subjectKey,predicates);
        }

        //get predicate key
        String predicateKey = triple.getPredicate().stringValue();

        //add or get object set
        Set<Map<String,String>> objects;
        if( predicates.containsKey(predicateKey) ) {
            objects = predicates.get(predicateKey);
        } else {
            objects = new HashSet<Map<String,String>>();
            predicates.put(predicateKey,objects);
        }

        //add objects
        objects.add(serializeNodeAsJson(triple.getObject()));

        return subjects;
    }


    /**
     * This method wraps triples (Subject,Property,Object) in a dataformat that is serializable
     * by Jackson (namely maps and sets).
     *
     * @param triples
     * @return
     */
    public static Map<String,?> serializeTriplesAsJson(Iterable<? extends Statement> triples) {

        Map<String,Map<String,Set<Map<String,String>>>> subjects = new HashMap<String,Map<String,Set<Map<String,String>>>>();
        for( Statement triple : triples ) {

            // get subject key
            String subjectKey = triple.getSubject().stringValue();

            //add or get predicate map
            Map<String,Set<Map<String,String>>> predicates;
            if( subjects.containsKey(subjectKey)) {
                predicates = subjects.get(subjectKey);
            } else {
                predicates = new HashMap<String,Set<Map<String,String>>>();
                subjects.put(subjectKey,predicates);
            }

            //get predicate key
            String predicateKey = triple.getPredicate().stringValue();

            //add or get object set
            Set<Map<String,String>> objects;
            if( predicates.containsKey(predicateKey) ) {
                objects = predicates.get(predicateKey);
            } else {
                objects = new HashSet<Map<String,String>>();
                predicates.put(predicateKey,objects);
            }

            //add objects
            objects.add(serializeNodeAsJson(triple.getObject()));

        }
        return subjects;
    }
}
