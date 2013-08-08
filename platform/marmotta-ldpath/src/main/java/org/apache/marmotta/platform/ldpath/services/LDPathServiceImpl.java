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
package org.apache.marmotta.platform.ldpath.services;

import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.model.programs.Program;
import org.apache.marmotta.platform.ldpath.api.LDPathService;
import org.apache.marmotta.platform.ldpath.api.AutoRegisteredLDPathFunction;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;

import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.api.transformers.NodeTransformer;
import org.apache.marmotta.ldpath.backend.sesame.SesameConnectionBackend;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.apache.marmotta.ldpath.model.Constants;
import org.apache.marmotta.ldpath.parser.Configuration;
import org.apache.marmotta.ldpath.parser.DefaultConfiguration;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import java.io.StringReader;
import java.util.*;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class LDPathServiceImpl implements LDPathService {

    @Inject
    private Logger log;

    @Inject
    private SesameService               sesameService;

    private Configuration<Value>        config;

    @Inject @Any
    private Instance<AutoRegisteredLDPathFunction> functions;

    @PostConstruct
    public void initialise() {
        log.info("initialising LDPath service ...");

        config = new DefaultConfiguration<Value>();

        for(AutoRegisteredLDPathFunction function : functions) {
            config.addFunction(Constants.NS_LMF_FUNCS + function.getLocalName(), function);
        }
    }


    /**
     * Register an LDPath function in this LDPath service. Can be used by other modules to plug in their own LDPath
     * functions as needed.
     *
     * @param function
     */
    @Override
    public void registerFunction(SelectorFunction<Value> function) {
        if (function instanceof AutoRegisteredLDPathFunction) {
            config.addFunction(((AutoRegisteredLDPathFunction) function).getLocalName(), function);
        } else {
            try {
                RepositoryConnection conn = sesameService.getConnection();
                try {
                    conn.begin();
                    SesameConnectionBackend backend = SesameConnectionBackend.withConnection(conn);
                    config.addFunction(Constants.NS_LMF_FUNCS + function.getPathExpression(backend), function);
                } finally {
                    conn.commit();
                    conn.close();
                }
            } catch (RepositoryException e) {
                log.warn("Could not add function:" + function.getSignature(), e);
            }
        }
    }

    @Override
    public void registerTransformer(String typeUri, NodeTransformer<?, Value> transformer) {
        config.addTransformer(typeUri, transformer);
    }


    /**
     * List all selector functions that are currently registered with LDPath.
     *
     * @return
     */
    @Override
    public Set<SelectorFunction<Value>> getFunctions() {
        Set<SelectorFunction<Value>> result = new HashSet<SelectorFunction<Value>>();
        result.addAll(config.getFunctions().values());

        return result;
    }

    @Override
    public Set<String> getTransformableTypes() {
        Set<String> t = new HashSet<String>();
        t.addAll(config.getTransformers().keySet());

        return t;
    }

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
    @Override
    public Collection<Value> pathQuery(Value context, String path, Map<String, String> namespaces) throws LDPathParseException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                SesameConnectionBackend backend = SesameConnectionBackend.withConnection(conn);
                LDPath<Value> ldpath = new LDPath<Value>(backend, config);

                return ldpath.pathQuery(context, path, namespaces);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            throw new LDPathParseException("LDPath evaluation failed", e);
        }
    }


    /**
     * Run a path program starting from the given context node and return the result as a collection of KiWiNodes for
     * each field occurring in the path progrm.
     *
     * @param context    the context node where to start with the path query
     * @param program    the path program to evaluate starting from the context node
     * @return           a map mapping from field names to the resulting collection of KiWiNodes for the field
     * @throws LDPathParseException when the path passed as argument could not be parsed
     */
    @Override
    public Map<String, Collection<?>> programQuery(Value context, String program) throws LDPathParseException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                SesameConnectionBackend backend = SesameConnectionBackend.withConnection(conn);
                LDPath<Value> ldpath = new LDPath<Value>(backend, config);

                return ldpath.programQuery(context, new StringReader(program));
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            throw new LDPathParseException("LDPath evaluation failed", e);
        }
    }

    /**
     * Run a path program over all resources in the triplestore matching the program's filter and return the result for
     * each respurce. Since this query can potentially return many results and take long, it is recommended to define
     * appropriate program filters for the query.
     *
     * @param program
     * @return
     * @throws org.apache.marmotta.ldpath.exception.LDPathParseException
     *
     */
    @Override
    public Map<Value, Map<String, Collection<?>>> programQuery(String program) throws LDPathParseException {
        Map<Value,  Map<String, Collection<?>>> result = new HashMap<>();
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                SesameConnectionBackend backend = SesameConnectionBackend.withConnection(conn);
                LDPath<Value> ldpath = new LDPath<Value>(backend, config);

                Program<Value> p = ldpath.parseProgram(new StringReader(program));

                // TODO: not very efficient, LDPath should support more efficient listing of resources based on filter
                for(Value context : ResourceUtils.listResources(conn)) {
                    if(p.getFilter().apply(backend, context, Collections.singleton(context))) {

                        Map<String,Collection<?>> binding = new HashMap<String, Collection<?>>();

                        for(FieldMapping<?,Value> mapping : p.getFields()) {
                            binding.put(mapping.getFieldName(),mapping.getValues(backend,context));
                        }
                        result.put(context,binding);
                    }
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            throw new LDPathParseException("LDPath evaluation failed", e);
        }
        return result;
    }
}
