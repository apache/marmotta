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
package org.apache.marmotta.ldpath.parser;

import com.google.common.collect.ImmutableMap;

import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.api.functions.TestFunction;
import org.apache.marmotta.ldpath.api.transformers.NodeTransformer;
import org.apache.marmotta.ldpath.model.Constants;
import org.apache.marmotta.ldpath.model.tests.functions.EqualTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A generic configuration object for LDPath parsers. Contains default settings for namespaces, functions,
 * and transformers to be used by all parser instances.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class Configuration<Node> {

    private static Logger log = LoggerFactory.getLogger(Configuration.class);

    /**
     * A map from namespace prefixes to namespace URIs
     */
    protected Map<String,String> namespaces;

    /**
     * A map from type URIs (e.g. XSD Schema Types) to LDPath transformer instances
     */
    protected Map<String, NodeTransformer<?,Node>> transformers;

    /**
     * A map from function URIs (e.g. LMF functions) to LDPath selector function instances
     */
    protected Map<String, SelectorFunction<Node>> functions;

    /**
     * A map from function URIs (e.g. LMF functions) to LDPath test function instances
     */
    protected Map<String, TestFunction<Node>> testFunctions;

    public Configuration() {
        namespaces   = new HashMap<String,String>();
        transformers = new HashMap<String, NodeTransformer<?, Node>>();
        functions    = new HashMap<String, SelectorFunction<Node>>();
        testFunctions = new HashMap<String, TestFunction<Node>>();
    }

    /**
     * Return an immutable map of the registered namespaces.
     *
     * @return an {@link ImmutableMap} of the registered namespaces.
     */
    public Map<String, String> getNamespaces() {
        return ImmutableMap.copyOf(namespaces);
    }

    /**
     * Return an immutable map of the registered transformers.
     *
     * @return an {@link ImmutableMap} of the registered transformers.
     */
    public Map<String, NodeTransformer<?, Node>> getTransformers() {
        return ImmutableMap.copyOf(transformers);
    }

    /**
     * Return an immutable map of the registered functions.
     *
     * @return an {@link ImmutableMap} of the registered {@link SelectorFunction}s.
     */
    public Map<String, SelectorFunction<Node>> getFunctions() {
        return ImmutableMap.copyOf(functions);
    }

    /**
     * Return an immutable map of the registered function tests.
     * 
     * @return an {@link ImmutableMap} of the registered {@link TestFunction}s.
     */
    public Map<String, TestFunction<Node>> getTestFunctions() {
        return ImmutableMap.copyOf(testFunctions);
    }

    /**
     * Add a namespace prefix to URI mapping to this configuration.
     *
     * @param prefix the namespace prefix to be used for the URI as a string
     * @param uri    the URI of the namespace to map to as a string
     */
    public void addNamespace(String prefix, String uri) {
        namespaces.put(prefix,uri);
    }

    /**
     * Remove the namespace with the prefix given as argument.
     *
     * @param prefix the namespace prefix to remove as a string
     */
    public void removeNamespace(String prefix) {
        namespaces.remove(prefix);
    }

    /**
     * Add a mapping from a URI to a {@link SelectorFunction} implementation to the configuration.
     * Registered selector functions will be available under their URIs during program parsing.
     * Function calls are not thread safe and the order of function calls is not predictable;
     * they should therefore not hold any state.
     * <p/>
     * Example:<br/>
     * <code>fn:concat(...)</code> is a mapping from http://www.newmedialab.at/lmf/functions/1.0/ to an instance
     * of {@link org.apache.marmotta.ldpath.model.functions.ConcatenateFunction}
     * <p/>
     * Note that currently you can only use the http://www.newmedialab.at/lmf/functions/1.0/ namespace for
     * registering functions.
     *
     * @param uri      the URI under which to register the selector function as a string
     * @param function an instance of the selector function implementation to use for this URI
     */
    public void addFunction(String uri, SelectorFunction<Node> function) {
        if(!uri.startsWith(Constants.NS_LMF_FUNCS)) {
            log.warn("Attempt to add a function from an external namespace: <{}>", uri);
            throw new IllegalArgumentException("namespaces other than http://www.newmedialab.at/lmf/functions/1.0/ are currently not supported");
        }
        functions.put(uri,function);
    }

    /**
     * Remove the function registered under the given URI.
     *
     * @param uri a string representing the URI under which the function has been registered.
     */
    public void removeFunction(String uri) {
        functions.remove(uri);
    }

    /**
     * Add a mapping from an URI to a {@link TestFunction} inplementation to the configuration.
     * Registered test functions will be available under their URIs during program parsing.
     * Function calls are not thread safe and the order of function calls in not predictable;
     * they should therefore not hold any state.
     * <p/>
     * Example:<br/>
     * <code>fn:eq(...)</code> is a mapping from http://www.newmedialab.at/lmf/functions/1.0/eq to an instance
     * of {@link EqualTest}
     * <p/>
     * Note that currently you can only use the http://www.newmedialab.at/lmf/functions/1.0/ namespace for
     * registering functions.
     * 
     * @param uri   the URI under which to register the selector function as a string
     * @param test  an instance of the test function implementation for this URI
     */
    public void addTestFunction(String uri, TestFunction<Node> test) {
        if(!uri.startsWith(Constants.NS_LMF_FUNCS)) {
            log.warn("Attempt to add a test function from an external namespace: <{}>", uri);
            throw new IllegalArgumentException("namespaces other than http://www.newmedialab.at/lmf/functions/1.0/ are currently not supported");
        }
        testFunctions.put(uri,test);
    }

    /**
     * Remove the test function registered under the given URI.
     *
     * @param uri a string representing the URI under which the test function has been registered.
     */
    public void removeTestFunction(String uri) {
        testFunctions.remove(uri);
    }

    /**
     * Add a mapping from a type URI to a {@link NodeTransformer}. The values of fields with a
     * type specification with this URI will be transformed to other Java types using this transformer.
     * <p/>
     * Example:<br/>
     * <code>... :: xsd:int</code> will transform the values of the field to Java integer values using
     * an instance of {@link org.apache.marmotta.ldpath.model.transformers.IntTransformer}
     *
     * @param uri         a string identifying the URI for which to register the transformer
     * @param transformer
     */
    public void addTransformer(String uri, NodeTransformer<?,Node> transformer) {
        transformers.put(uri,transformer);
    }

    /**
     * Remove the transformer registered with the URI given as argument
     * @param uri a string representing the URI under which the transformer is registered
     */
    public void removeTransformer(String uri) {
        transformers.remove(uri);
    }
}
