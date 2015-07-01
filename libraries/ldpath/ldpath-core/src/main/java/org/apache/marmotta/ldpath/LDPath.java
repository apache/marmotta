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
package org.apache.marmotta.ldpath;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.apache.marmotta.ldpath.api.transformers.NodeTransformer;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.model.programs.Program;
import org.apache.marmotta.ldpath.parser.Configuration;
import org.apache.marmotta.ldpath.parser.DefaultConfiguration;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Main class providing query functionality for the different RDF backends.
 *
 * @param <Node> the node type used by the backend
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDPath<Node> {

    private RDFBackend<Node> backend;

    private HashSet<SelectorFunction<Node>> functions;

    private HashMap<String,NodeTransformer<?,Node>> transformers;

    private Configuration<Node> config;

    /**
     * Initialise a new LDPath instance for querying the backend passed as argument with the default configuration.
     * @param backend
     */
    public LDPath(RDFBackend<Node> backend) {
        this(backend,new DefaultConfiguration<Node>());
    }

    /**
     * Initialise a new LDPath instance for querying the backend passed as argument with a custom configuration
     * for default functions, transformers and namespaces.
     *
     * @param backend
     * @param config
     */
    public LDPath(RDFBackend<Node> backend, Configuration<Node> config) {
        this.backend      = backend;
        this.functions    = new HashSet<SelectorFunction<Node>>();
        this.transformers = new HashMap<String, NodeTransformer<?, Node>>();
        this.config       = config;
    }

    /**
     * Execute a single path query starting from the given context node and return a collection of nodes resulting
     * from the selection. Default namespaces (rdf, rdfs, skos, dc, foaf) are added automatically, further namespaces
     * need to be passed as arguments.
     * <p/>
     * Paths need to conform to the RdfPath Selector syntax described at
     * <a href="http://code.google.com/p/kiwi/wiki/RdfPathLanguage#Path_Selectors">the wiki</a>.
     * For example, the following selection would select the names of all friends:
     * <p/>
     * <code>
     * foaf:knows / foaf:name
     * </code>
     * <p/>
     * Note that since this method returns a collection of nodes, no transformations can be used.
     *
     * @param context the context node where to start the path query
     * @param path  the LDPath path specification
     * @param namespaces an optional map mapping namespace prefixes to URIs (used for lookups of prefixes used in the path);
     *                   can be null
     * @return a collection of nodes
     * @throws LDPathParseException when the path passed as argument is not valid
     */
    public Collection<Node> pathQuery(Node context, String path, Map<String, String> namespaces) throws LDPathParseException {
        LdPathParser<Node> parser = new LdPathParser<Node>(backend,config,new StringReader(path));
        for(SelectorFunction<Node> function : functions) {
            parser.registerFunction(function);
        }
        for(String typeUri : transformers.keySet()) {
            parser.registerTransformer(typeUri,transformers.get(typeUri));
        }

        try {
            NodeSelector<Node> selector = parser.parseSelector(namespaces);

            return selector.select(backend,context,null,null);

        } catch (ParseException e) {
            throw new LDPathParseException("error while parsing path expression",e);
        }

    }


    /**
     * Execute a single path query starting from the given context node and return a collection of nodes resulting
     * from the selection. In addition, the paths leading to the individual results are returned in the map passed as
     * argument "paths".
     * <p/>
     * Default namespaces (rdf, rdfs, skos, dc, foaf) are added automatically, further namespaces
     * need to be passed as arguments.
     * <p/>
     * Paths need to conform to the RdfPath Selector syntax described at
     * <a href="http://code.google.com/p/kiwi/wiki/RdfPathLanguage#Path_Selectors">the wiki</a>.
     * For example, the following selection would select the names of all friends:
     * <p/>
     * <code>
     * foaf:knows / foaf:name
     * </code>
     * <p/>
     * Note that since this method returns a collection of nodes, no transformations can be used.
     *
     * @param context the context node where to start the path query
     * @param path  the LDPath path specification
     * @param namespaces an optional map mapping namespace prefixes to URIs (used for lookups of prefixes used in the path);
     *                   can be null
     * @return a collection of nodes
     * @throws LDPathParseException when the path passed as argument is not valid
     */
    public Collection<Node> pathQuery(Node context, String path, Map<String, String> namespaces, Map<Node,List<Node>> paths) throws LDPathParseException {
        LdPathParser<Node> parser = new LdPathParser<Node>(backend,config,new StringReader(path));
        for(SelectorFunction<Node> function : functions) {
            parser.registerFunction(function);
        }
        for(String typeUri : transformers.keySet()) {
            parser.registerTransformer(typeUri,transformers.get(typeUri));
        }

        try {
            NodeSelector<Node> selector = parser.parseSelector(namespaces);

            return selector.select(backend, context, ImmutableList.<Node> of(), paths);

        } catch (ParseException e) {
            throw new LDPathParseException("error while parsing path expression",e);
        }

    }



    /**
     * Execute a single path query starting from the given context node and return a collection of nodes resulting
     * from the selection. Default namespaces (rdf, rdfs, skos, dc, foaf) are added automatically, further namespaces
     * need to be passed as arguments.
     * <p/>
     * Paths need to conform to the RdfPath Selector syntax described at
     * <a href="http://code.google.com/p/kiwi/wiki/RdfPathLanguage#Path_Selectors">the wiki</a>.
     * For example, the following selection would select the names of all friends:
     * <p/>
     * <code>
     * foaf:knows / foaf:name
     * </code>
     * <p/>
     * Note that since this method returns a collection of nodes, no transformations can be used.
     *
     * @param context the context node where to start the path query
     * @param path  the LDPath path specification
     * @param namespaces an optional map mapping namespace prefixes to URIs (used for lookups of prefixes used in the path);
     *                   can be null
     * @return a collection of nodes
     * @throws LDPathParseException when the path passed as argument is not valid
     */
    public <T> Collection<T> pathTransform(Node context, String path, Map<String, String> namespaces) throws LDPathParseException {
        LdPathParser<Node> parser = new LdPathParser<Node>(backend,config,new StringReader(path));
        for(SelectorFunction<Node> function : functions) {
            parser.registerFunction(function);
        }
        for(String typeUri : transformers.keySet()) {
            parser.registerTransformer(typeUri,transformers.get(typeUri));
        }

        try {
            FieldMapping<T,Node> mapping = parser.parseRule(namespaces);

            return mapping.getValues(backend, context);

        } catch (ParseException e) {
            throw new LDPathParseException("error while parsing path expression",e);
        }

    }

    /**
     * Evaluate a path program passed as argument starting from the given context node and return a mapping for
     * each field in the program to the selected values.
     *
     * @param context
     * @param program
     * @return
     * @throws LDPathParseException
     */
    public Map<String,Collection<?>> programQuery(Node context, Reader program) throws LDPathParseException {
        LdPathParser<Node> parser = new LdPathParser<Node>(backend,config,program);
        for(SelectorFunction<Node> function : functions) {
            parser.registerFunction(function);
        }
        for(String typeUri : transformers.keySet()) {
            parser.registerTransformer(typeUri, transformers.get(typeUri));
        }

        try {
            Program<Node> p = parser.parseProgram();

            Map<String,Collection<?>> result = new HashMap<String, Collection<?>>();

            for(FieldMapping<?,Node> mapping : p.getFields()) {
                result.put(mapping.getFieldName(),mapping.getValues(backend,context));
            }

            return result;

        } catch (ParseException e) {
            throw new LDPathParseException("error while parsing path expression",e);
        }
    }

    /**
     * Parse a program passed as argument and return it for further use.
     *
     * @param program a reader containing the program in LDPath syntax
     * @return the parsed program
     * @throws LDPathParseException
     */
    public Program<Node> parseProgram(Reader program) throws LDPathParseException {
        LdPathParser<Node> parser = new LdPathParser<Node>(backend,config,program);
        for(SelectorFunction<Node> function : functions) {
            parser.registerFunction(function);
        }
        for(String typeUri : transformers.keySet()) {
            parser.registerTransformer(typeUri, transformers.get(typeUri));
        }

        try {
            return parser.parseProgram();
        } catch (ParseException e) {
            throw new LDPathParseException("error while parsing path program",e);
        }
    }

    /**
     * Register a selector function to be used in LDPath. Use this method in your own
     * projects to register custom selector functions.
     * @param function
     */
    public void registerFunction(SelectorFunction<Node> function) {
        functions.add(function);
    }

    /**
     * Return the collection of selector functions registered with this LDPath instance.
     *
     * @return
     */
    public Set<SelectorFunction<Node>> getFunctions() {
        return ImmutableSet.copyOf(functions);
    }

    /**
     * Register a result transformer for a type URI. Use this method in your own projects
     * to register custom result transformers.
     *
     * @param typeUri a URI identifying the type for which to use this transformer; can be specified in path programs
     * @param transformer instance of a node transformer
     */
    public void registerTransformer(String typeUri, NodeTransformer<?,Node> transformer) {
        transformers.put(typeUri,transformer);
    }

    public Map<String, NodeTransformer<?,Node>> getTransformers() {
        return ImmutableMap.copyOf(transformers);
    }


    /**
     * Return the configuration underlying this LDPath instance.
     * @return
     */
    public Configuration<Node> getConfig() {
        return config;
    }
}
