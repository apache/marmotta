/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.caching.sail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.UnionIteration;
import org.apache.marmotta.commons.sesame.tripletable.IntArray;
import org.apache.marmotta.kiwi.caching.iteration.BufferingIteration;
import org.apache.marmotta.kiwi.caching.iteration.CachingIteration;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.infinispan.Cache;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.UpdateContext;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.*;
import java.nio.IntBuffer;
import java.util.*;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiCachingSailConnection extends NotifyingSailConnectionWrapper {

    private static Logger log = LoggerFactory.getLogger(KiWiCachingSailConnection.class);

    private Cache<IntArray,List<Statement>> queryCache;

    // a dummy default context to work around the double meaning of the null value
    private final static URI defaultContext = new URIImpl("http://marmotta.apache.org/contexts/default");

    private int limit = 150;

    public KiWiCachingSailConnection(NotifyingSailConnection wrappedCon, Cache<IntArray, List<Statement>> queryCache, int limit) {
        super(wrappedCon);

        this.queryCache = queryCache;
        this.limit      = limit;

    }


    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(final Resource subj, final URI pred, final Value obj, final boolean includeInferred, final Resource... contexts) throws SailException {
        List<Iteration<? extends Statement, SailException>> cResults = new ArrayList<>(contexts.length + 1);
        for(final Resource context : resolveContexts(contexts)) {
            cResults.add(new CachingIteration<>(
                    new CachingIteration.CacheFunction<Statement>() {
                        @Override
                        public List<Statement> getResult() {
                            return listTriples(subj,pred,obj,context, includeInferred);
                        }

                        @Override
                        public void cacheResult(List<Statement> buffer) {
                            cacheTriples(subj,pred,obj,context,includeInferred,buffer);
                        }
                    },
                    new CachingIteration.BufferingIterationProducer<Statement, SailException>() {
                        @Override
                        public BufferingIteration<Statement, SailException> getIteration() throws SailException {
                            return new BufferingIteration<>(limit, KiWiCachingSailConnection.super.getStatements(subj, pred, obj, includeInferred, contexts));
                        }
                    }
            ));
        }

        return new UnionIteration<Statement, SailException>(cResults);

    }

    @Override
    public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        tripleUpdated(subj, pred, obj, resolveContexts(contexts));

        super.addStatement(subj, pred, obj, contexts);
    }

    @Override
    public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        // TODO: too aggressive, but currently we cannot remove with wildcards
        queryCache.clear();

        super.removeStatements(subj, pred, obj, contexts);
    }

    @Override
    public void addStatement(UpdateContext modify, Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        tripleUpdated(subj, pred, obj, resolveContexts(contexts));

        super.addStatement(modify, subj, pred, obj, contexts);
    }

    @Override
    public void removeStatement(UpdateContext modify, Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        // TODO: too aggressive, but currently we cannot remove with wildcards
        queryCache.clear();

        super.removeStatement(modify, subj, pred, obj, contexts);
    }


    @Override
    public void clear(Resource... contexts) throws SailException {
        // TODO: too aggressive, but currently we cannot remove with wildcards
        queryCache.clear();

        super.clear(contexts);
    }

    @Override
    public void begin() throws SailException {
        super.begin();

        try {
            queryCache.getAdvancedCache().getTransactionManager().begin();
        } catch (NotSupportedException | SystemException e) {
            log.error("error starting cache transaction: ",e);
        }
    }

    @Override
    public void commit() throws SailException {
        try {
            queryCache.getAdvancedCache().getTransactionManager().commit();
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException e) {
            log.error("error committing cache transaction: ",e);
        }

        super.commit();
    }

    @Override
    public void rollback() throws SailException {
        try {
            queryCache.getAdvancedCache().getTransactionManager().rollback();
        } catch (SystemException e) {
            log.error("error rolling back cache transaction: ",e);
        }

        super.rollback();
    }


    @Override
    public void close() throws SailException {
        try {
            queryCache.getAdvancedCache().getTransactionManager().suspend();
        } catch (SystemException e) {
            log.error("error suspending transaction",e);
        }

        super.close();
    }

    private List<Resource> resolveContexts(Resource... contexts) {
        if(contexts.length == 0) {
            return Collections.singletonList((Resource) defaultContext);
        } else  {
            return Lists.newArrayList(contexts);
        }
    }

    /**
     * Look up a triple query in the query cache. Returns the result set if the query is found in the cache, returns
     * null if the query is not found.
     *
     * @param subject  the subject of the triples to list or null for wildcard
     * @param property the property of the triples to list or null for wildcard
     * @param object   the object of the triples to list or null for wildcard
     * @param context  the context/knowledge space of the triples to list or null for all spaces
     * @param inferred if true, inferred triples are included in the result; if false not
     * @return the result set if the query is found in the cache, returns null if the query is not found
     */
    @SuppressWarnings("unchecked")
    private List<Statement> listTriples(Resource subject, URI property, Value object, Resource context, boolean inferred) {
        IntArray key = createCacheKey(subject,property,object,context,inferred);
        if(queryCache.get(key) != null) return queryCache.get(key);
        else
            return null;
    }


    /**
     * Cache the result of a triple query in the query cache.
     *
     * @param subject  the subject of the triples to list or null for wildcard
     * @param property the property of the triples to list or null for wildcard
     * @param object   the object of the triples to list or null for wildcard
     * @param context  the context/knowledge space of the triples to list or null for all spaces
     * @param inferred if true, inferred triples are included in the result; if false not
     * @param result   the result of the triple query to cache
     */
    private void cacheTriples(Resource subject, URI property, Value object, Resource context, boolean inferred, List<Statement> result) {

        // cache the query result
        IntArray key = createCacheKey(subject,property,object,context,inferred);
        queryCache.putAsync(key, result);

        // cache the nodes of the triples and the triples themselves
        Set<Value> nodes = new HashSet<Value>();
        for(Statement stmt : result) {
            if(stmt instanceof KiWiTriple) {
                KiWiTriple triple = (KiWiTriple)stmt;
                Collections.addAll(nodes, new Value[]{triple.getSubject(), triple.getObject(), triple.getPredicate(), triple.getContext()});
                queryCache.putAsync(createCacheKey(triple.getSubject(), triple.getPredicate(), triple.getObject(), triple.getContext(), triple.isInferred()), ImmutableList.of(stmt));
            }
        }

        // special optimisation: when only the subject (and optionally context) is given, we also fill the caches for
        // all property values
        if(subject != null && property == null && object == null) {
            HashMap<URI,List<Statement>> properties = new HashMap<>();
            for(Statement triple : result) {
                List<Statement> values = properties.get(triple.getPredicate());
                if(values == null) {
                    values = new LinkedList<>();
                    properties.put(triple.getPredicate(),values);
                }
                values.add(triple);
            }
            for(Map.Entry<URI,List<Statement>> entry : properties.entrySet()) {
                IntArray key2 = createCacheKey(subject,entry.getKey(),null,context,inferred);
                queryCache.putAsync(key2, entry.getValue());
            }
        }


    }




    /**
     * Clear all contents of the query cache.
     */
    private void clearAll() {
        queryCache.clear();
    }


    /**
     * Notify the cache that the triple passed as argument has been updated and that all cache entries affected by
     * the triple update need to be cleared.
     *
     */
    private void tripleUpdated(Resource subject, URI predicate, Value object, Iterable<Resource> contexts) {
        queryCache.remove(createCacheKey(null,null,null,null,false));
        queryCache.remove(createCacheKey(null,null,null,null,true));

        // remove all possible combinations of this triple as they may appear in the cache
        queryCache.remove(createCacheKey(subject,null,null,null,false));
        queryCache.remove(createCacheKey(subject,null,null,null,true));
        queryCache.remove(createCacheKey(null,predicate,null,null,false));
        queryCache.remove(createCacheKey(null,predicate,null,null,true));
        queryCache.remove(createCacheKey(null,null,object,null,false));
        queryCache.remove(createCacheKey(null,null,object,null,true));

        queryCache.remove(createCacheKey(subject,predicate,null,null,false));
        queryCache.remove(createCacheKey(subject,predicate,null,null,true));
        queryCache.remove(createCacheKey(subject,null,object,null,false));
        queryCache.remove(createCacheKey(subject,null,object,null,true));
        queryCache.remove(createCacheKey(null,predicate,object,null,false));
        queryCache.remove(createCacheKey(null,predicate,object,null,true));


        queryCache.remove(createCacheKey(subject,predicate,object,null,false));
        queryCache.remove(createCacheKey(subject,predicate,object,null,true));

        for(Resource context : contexts) {
            queryCache.remove(createCacheKey(null,null,null,context,false));
            queryCache.remove(createCacheKey(null,null,null,context,true));
            queryCache.remove(createCacheKey(subject,null,null,context,false));
            queryCache.remove(createCacheKey(subject,null,null,context,true));
            queryCache.remove(createCacheKey(null,predicate,null,context,false));
            queryCache.remove(createCacheKey(null,predicate,null,context,true));
            queryCache.remove(createCacheKey(null,null,object,context,false));
            queryCache.remove(createCacheKey(null,null,object,context,true));

            queryCache.remove(createCacheKey(subject,predicate,null,context,false));
            queryCache.remove(createCacheKey(subject,predicate,null,context,true));
            queryCache.remove(createCacheKey(subject,null,object,context,false));
            queryCache.remove(createCacheKey(subject,null,object,context,true));
            queryCache.remove(createCacheKey(null,predicate,object,context,false));
            queryCache.remove(createCacheKey(null,predicate,object,context,true));

            queryCache.remove(createCacheKey(subject,predicate,object,context,false));
            queryCache.remove(createCacheKey(subject,predicate,object,context,true));
        }
    }


    private static IntArray createCacheKey(Resource subject, URI property, Value object, Resource context, boolean inferred){

        // the cache key is generated by appending the bytes of the hashcodes of subject, property, object, context and inferred and
        // storing them as a BigInteger; generating the cache key should thus be very efficient

        int s = subject != null ? subject.hashCode() : Integer.MIN_VALUE;
        int p = property != null ? property.hashCode() : Integer.MIN_VALUE;
        int o = object != null ? object.hashCode() : Integer.MIN_VALUE;
        int c = context != null ? context.hashCode() : Integer.MIN_VALUE;

        IntBuffer bb = IntBuffer.allocate(5);
        bb.put(s);
        bb.put(p);
        bb.put(o);
        bb.put(c);
        bb.put( (byte) (inferred ? 1 : 0) );

        return new IntArray(bb.array());

    }


}
