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
package org.apache.marmotta.kiwi.reasoner.parser;

import org.apache.marmotta.kiwi.reasoner.model.program.Program;
import org.apache.marmotta.kiwi.reasoner.model.program.ResourceField;
import org.apache.marmotta.kiwi.reasoner.model.program.Rule;
import org.apache.marmotta.kiwi.reasoner.model.program.VariableField;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Base class for the JavaCC parser with support methods
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class KWRLProgramParserBase {

    protected static final Logger log = LoggerFactory.getLogger(KWRLProgramParserBase.class);


    protected ValueFactory valueFactory;

    protected Map<String,VariableField> variables;
    protected Map<String,ResourceField> resources;

    /**
     * A map mapping from namespace prefix to namespace URI
     */
    protected Map<String,String> namespaces;


    protected KWRLProgramParserBase() {
        namespaces = new HashMap<String, String>();
    }

    protected ValueFactory getValueFactory() {
        return valueFactory;
    }

    protected void setValueFactory(ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    protected void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Short hand for parsing a program from a string using a given value factory.
     *
     * @param content  the program represented as string
     * @param vf       the value factory to use by the parser for creating resources and literals
     * @return         the parsed KWRL program
     * @throws ParseException
     */
    public static Program parseProgram(String content, ValueFactory vf) throws ParseException {
        KWRLProgramParserBase parser = new KWRLProgramParser(vf,content);
        return parser.parseProgram();
    }


    /**
     * Short hand for parsing a rule from a string using a given dictionary of namespaces and a value factory.
     * @param content  the rule represented as string; needs to use fully-qualified URIs for resources
     * @param vf       the value factory to use by the parser for creating resources and literals
     * @return         the parsed rule
     * @throws ParseException
     */
    public static Rule parseRule(String content, ValueFactory vf) throws ParseException {
        return parseRule(content, null, vf);
    }

    /**
     * Short hand for parsing a rule from a string using a given dictionary of namespaces and a value factory.
     * @param content  the rule represented as string
     * @param namespaces a map of namespaces, mapping from prefix to namespace URI
     * @param vf       the value factory to use by the parser for creating resources and literals
     * @return         the parsed rule
     * @throws ParseException
     */
    public static Rule parseRule(String content, Map<String,String> namespaces, ValueFactory vf) throws ParseException {
        KWRLProgramParserBase parser = new KWRLProgramParser(vf,content);
        if(namespaces != null) {
            parser.setNamespaces(namespaces);
        }
        return parser.parseRule();
    }

    public synchronized Program parseProgram() throws ParseException {
        return Program();
    }

    public synchronized Rule parseRule(Map<String,String> namespaces) throws ParseException {
        this.namespaces = namespaces;

        return Rule();
    }


    public synchronized Rule parseRule() throws ParseException {
        return Rule();
    }

    protected   URI resolveResource(String uri) {
        return valueFactory.createURI(uri);
    }

    protected Literal resolveLiteral(Object content, Locale loc, String typeUri) {
        if(typeUri != null) {
            return valueFactory.createLiteral(content.toString(),valueFactory.createURI(typeUri));
        } else if(loc != null) {
            return valueFactory.createLiteral(content.toString(), loc.getLanguage());
        } else {
            return valueFactory.createLiteral(content.toString());
        }
    }

    protected VariableField getVariable(String name) {
        if(variables != null && variables.get(name) != null) {
            return variables.get(name);
        } else {
            VariableField result = new VariableField(name);
            variables.put(name,result);
            return result;
        }
    }

    protected ResourceField getResource(String uri) {
        if(resources != null && resources.get(uri) != null) {
            return resources.get(uri);
        } else {
            ResourceField result = new ResourceField(resolveResource(uri));
            resources.put(uri,result);
            return result;
        }
    }

    protected ResourceField getResourceByNS(String nsUri) throws ParseException {
        String[] components = nsUri.split(":");
        if(namespaces.get(components[0]) == null) {
            throw new ParseException("namespace "+components[0]+" could not be found");
        } else {
            String uri = namespaces.get(components[0])+components[1];
            return getResource(uri);
        }
    }

    protected  void startRule() {
        variables = new HashMap<String,VariableField>();
        resources = new HashMap<String,ResourceField>();
    }

    protected  void endRule() {
        variables = null;
        resources = null;
    }



    /**
     * Parse the input as KWRL program. Implemented by JavaCC-generated parser
     *
     * @return
     */
    public abstract Program Program() throws ParseException;

    /**
     * Parse the input as KWRL rule. Implemented by JavaCC-generated parser
     *
     * @return
     */
    public abstract Rule Rule() throws ParseException;

}
