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
package org.apache.marmotta.platform.ldpath.api;


import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.api.transformers.NodeTransformer;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.openrdf.model.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface LDPathService {

    /**
     * Register an LDPath function in this LDPath service. Can be used by other modules to plug in
     * their own LDPath functions as needed.
     * 
     * @param function {@link SelectorFunction} to register.
     */
    public void registerFunction(SelectorFunction<Value> function);


    /**
     * List all selector functions that are currently registered with LDPath.
     */
    public Set<SelectorFunction<Value>> getFunctions();

    /**
     * Run a path query starting from the given context node and return the result as a collection of KiWiNodes. The
     * namespaces argument is optional and contains a map from prefixes to namespace URIs that can be used in the
     * path expression.
     *
     * @param context    the context node where to start with the path query
     * @param path       the path query to evaluate starting from the context node
     * @param namespaces optional map from namespace prefixes to namespace URIs
     * @return           a collection of KiWiNodes
     * @throws LDPathParseException when the path passed as argument could not be parsed
     */
    public Collection<Value> pathQuery(Value context, String path, Map<String, String> namespaces) throws LDPathParseException;

    /**
     * Run a path program starting from the given context node and return the result as a collection of KiWiNodes for
     * each field occurring in the path progrm.
     *
     * @param context    the context node where to start with the path query
     * @param program    the path program to evaluate starting from the context node
     * @return           a map mapping from field names to the resulting collection of KiWiNodes for the field
     * @throws org.apache.marmotta.ldpath.exception.LDPathParseException when the path passed as argument could not be parsed
     */
    public Map<String, Collection<?>> programQuery(Value context, String program) throws LDPathParseException;


    /**
     * Run a path program over all resources in the triplestore matching the program's filter and return the result for
     * each respurce. Since this query can potentially return many results and take long, it is recommended to define
     * appropriate program filters for the query.
     *
     * @param program
     * @return
     * @throws LDPathParseException
     */
    public Map<Value,Map<String,Collection<?>>> programQuery(String program) throws LDPathParseException;

    /**
     * Register a result transformer for a type URI. Use this method in your own projects
     * to register custom result transformers.
     * 
     * @param typeUri a URI identifying the type for which to use this transformer; can be specified
     *            in path programs
     * @param transformer instance of a node transformer
     */
    public void registerTransformer(String typeUri, NodeTransformer<?, Value> transformer);

    /**
     * List all types that have a transformer registered.
     */
    public Set<String> getTransformableTypes();
}
