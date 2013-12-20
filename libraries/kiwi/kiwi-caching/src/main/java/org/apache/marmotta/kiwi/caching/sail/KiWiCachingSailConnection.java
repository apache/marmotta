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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.UnionIteration;
import org.apache.geronimo.transaction.manager.TransactionImpl;
import org.apache.marmotta.commons.sesame.tripletable.IntArray;
import org.apache.marmotta.kiwi.caching.iteration.BufferingIteration;
import org.apache.marmotta.kiwi.caching.iteration.CachingIteration;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.sail.KiWiSailConnection;
import org.infinispan.Cache;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.*;
import java.nio.IntBuffer;
import java.sql.SQLException;
import java.util.*;

/**
 * A sail connection with Infinispan caching support. It will dynamically cache getStatements results up to a certain
 * result size and invalidate the cache on updates.
 *
 * <p/>
 * Since Infinispan uses JTA for transaction management, we need to align Sesame transactions with JTA. JTA transactions
 * are associated per-thread, while Sesame transactions are per-connection. This makes this combination a bit tricky:
 * every time a relevant method on the sesame connection is called we need to suspend the existing thread transaction
 * and resume the connection that is associated with the connection.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiCachingSailConnection extends NotifyingSailConnectionWrapper implements SailConnectionListener {

    private static Logger log = LoggerFactory.getLogger(KiWiCachingSailConnection.class);

    private Cache<Long,long[]> queryCache;

    // a dummy default context to work around the double meaning of the null value
    private final static URI defaultContext = new URIImpl("http://marmotta.apache.org/contexts/default");

    private int limit = 150;

    private Transaction tx;

    private long connectionId;

    private static long connectionIdCounter = 0;

    private KiWiConnection kiWiConnection;

    public KiWiCachingSailConnection(NotifyingSailConnection wrappedCon, Cache<Long, long[]> queryCache, int limit) {
        super(wrappedCon);

        this.queryCache = queryCache;
        this.limit      = limit;
        this.kiWiConnection = getKiWiConnection(wrappedCon);

        this.addConnectionListener(this);

        connectionId = ++connectionIdCounter;

    }


    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(final Resource subj, final URI pred, final Value obj, final boolean includeInferred, final Resource... contexts) throws SailException {
        if(tx != null) {
            log.debug("CONN({}) LIST: listing statements for transaction: {}", connectionId, ((TransactionImpl) tx).getTransactionKey());
        } else {
            log.debug("CONN({}) LIST: listing statements (no transaction)", connectionId);
        }
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
                            log.debug("CONN({}) CACHE: caching result for query ({},{},{},{},{}): {}", connectionId, subj, pred, obj, context, includeInferred, buffer);
                            cacheTriples(subj, pred, obj, context, includeInferred, buffer);
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


    /**
     * Notifies the listener that a statement has been added in a transaction
     * that it has registered itself with.
     *
     * @param st The statement that was added.
     */
    @Override
    public void statementAdded(Statement st) {
        resumeTransaction();
        log.debug("CONN({}) ADD: updating cache for statement {} (transaction: {})", connectionId, st, ((TransactionImpl) tx).getTransactionKey());
        if(st.getContext() == null) {
            tripleUpdated(st.getSubject(), st.getPredicate(), st.getObject(), Collections.singleton((Resource)defaultContext));
        } else {
            tripleUpdated(st.getSubject(), st.getPredicate(), st.getObject(), Collections.singleton(st.getContext()));
        }
    }

    /**
     * Notifies the listener that a statement has been removed in a transaction
     * that it has registered itself with.
     *
     * @param st The statement that was removed.
     */
    @Override
    public void statementRemoved(Statement st) {
        log.debug("CONN({}) DEL: updating cache for statement {} (transaction: {})", connectionId, st, ((TransactionImpl)tx).getTransactionKey());
        resumeTransaction();
        if(st.getContext() == null) {
            tripleUpdated(st.getSubject(), st.getPredicate(), st.getObject(), Collections.singleton((Resource)defaultContext));
        } else {
            tripleUpdated(st.getSubject(), st.getPredicate(), st.getObject(), Collections.singleton(st.getContext()));
        }
    }

    @Override
    public void begin() throws SailException {
        super.begin();

        resumeTransaction();
    }

    @Override
    public void commit() throws SailException {
        TransactionManager txmgr = queryCache.getAdvancedCache().getTransactionManager();
        try {
            resumeTransaction();
            log.debug("CONN({}) COMMIT: transaction: {}", connectionId, ((TransactionImpl) tx).getTransactionKey());
            txmgr.commit();
            closeTransaction();
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException e) {
            log.error("error committing cache transaction: ", e);
        }

        super.commit();
    }

    @Override
    public void rollback() throws SailException {
        TransactionManager txmgr = queryCache.getAdvancedCache().getTransactionManager();
        try {
            resumeTransaction();
            txmgr.rollback();
            closeTransaction();
        } catch (SystemException e) {
            log.error("error rolling back cache transaction: ",e);
        }

        super.rollback();
    }

    @Override
    public void close() throws SailException {
        closeTransaction();

        super.close();
    }

    private void resumeTransaction() {
        TransactionManager txmgr = queryCache.getAdvancedCache().getTransactionManager();
        try {
            // cases:
            // 1. there is a transaction in this connection, the transaction is active, and associated with the current
            //    thread -> nothing to do
            // 2. there is a transaction in this connection, the transaction is active, bit another transactionis 
            //    associated with the current thread -> suspend thread transaction, resume connection transaction
            // 3. there is no transaction in this connection, or the transaction in this connection is invalid
            //    -> create and start new transaction
            if(tx != null && tx.getStatus() == Status.STATUS_ACTIVE && txmgr.getTransaction() == tx) {
                log.debug("CONN({}) RESUME: using active transaction: {}, status {}", connectionId, ((TransactionImpl)tx).getTransactionKey(), tx.getStatus());
            } else if(tx != null && tx.getStatus() == Status.STATUS_ACTIVE && txmgr.getTransaction() != tx) {
                txmgr.suspend();
                txmgr.resume(tx);

                log.debug("CONN({}) RESUME: resumed transaction: {}, status {}", connectionId, ((TransactionImpl)tx).getTransactionKey(), tx.getStatus());
            } else {
                if(txmgr.getTransaction() != null) {
                    Transaction old = txmgr.suspend();
                    log.debug("CONN({}) BEGIN: suspended transaction not belonging to this connection: {}", connectionId, ((TransactionImpl)old).getTransactionKey());
                }
                txmgr.begin();
                tx = txmgr.getTransaction();

                log.debug("CONN({}) BEGIN: created and started new transaction: {}", connectionId, ((TransactionImpl)tx).getTransactionKey());

            }


        } catch (NotSupportedException | SystemException | InvalidTransactionException e) {
            log.error("error resuming transaction");
        }
    }

    private void closeTransaction() {
        TransactionManager txmgr = queryCache.getAdvancedCache().getTransactionManager();
        try {
            if(tx != null && txmgr.getTransaction() == tx) {
                log.debug("CONN({}) CLOSE: closing transaction: {}", connectionId, ((TransactionImpl)tx).getTransactionKey());
                if(tx.getStatus() == Status.STATUS_ACTIVE) {
                    tx.commit();
                }
                txmgr.suspend();
                tx = null;
            }
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException e) {
            log.error("error while closing transaction", e);
        }
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
        boolean implicitTx = tx == null;
        resumeTransaction();

        IntArray key = createCacheKey(subject,property,object,context,inferred);
        try {
            long[] ids = queryCache.get(key.longHashCode());
            if(ids == null) {
                return null;
            } else {
                ArrayList<Statement> statements = new ArrayList<>(ids.length);
                for(long id : ids) {
                    try {
                        statements.add(kiWiConnection.loadTripleById(id));
                    } catch (SQLException e) {
                        log.warn("could not load triple from database: {}",id);
                    }
                }
                return statements;
            }
        } finally {
            if(implicitTx) {
                closeTransaction();
            }
        }
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
    private void cacheTriples(final Resource subject, final URI property, final Value object, final Resource context, boolean inferred, List<Statement> result) {
        boolean implicitTx = tx == null;

        resumeTransaction();

        try {
            // cache the query result
            IntArray key = createCacheKey(subject,property,object,context,inferred);
            long[] data = new long[result.size()];
            for(int i=0; i<result.size(); i++) {
                Statement stmt = result.get(i);
                if(stmt instanceof KiWiTriple) {
                    data[i] = ((KiWiTriple) stmt).getId();
                }
            }
            queryCache.put(key.longHashCode(), data);

            // cache the nodes of the triples and the triples themselves
            Set<Value> nodes = new HashSet<Value>();
            for(Statement stmt : result) {
                if(stmt instanceof KiWiTriple) {
                    KiWiTriple triple = (KiWiTriple)stmt;
                    Collections.addAll(nodes, new Value[]{triple.getSubject(), triple.getObject(), triple.getPredicate(), triple.getContext()});
                    queryCache.put(createCacheKey(triple.getSubject(), triple.getPredicate(), triple.getObject(), triple.getContext(), triple.isInferred()).longHashCode(), new long[] {triple.getId()});
                }
            }

            // special optimisation: when only the subject (and optionally context) is given, we also fill the caches for
            // all property values
            if(subject != null && property == null && object == null) {
                HashMap<URI,ArrayList<Long>> properties = new HashMap<>();
                for(Statement triple : result) {
                    ArrayList<Long> values = properties.get(triple.getPredicate());
                    if(values == null) {
                        values = new ArrayList<>();
                        properties.put(triple.getPredicate(),values);
                    }
                    if(triple instanceof KiWiTriple) {
                        values.add(((KiWiTriple) triple).getId());
                    }
                }
                for(Map.Entry<URI,ArrayList<Long>> entry : properties.entrySet()) {
                    IntArray key2 = createCacheKey(subject,entry.getKey(),null,context,inferred);
                    long[] dvalues = new long[entry.getValue().size()];
                    for(int i=0; i<entry.getValue().size(); i++) {
                        dvalues[i] = entry.getValue().get(i);
                    }
                    queryCache.put(key2.longHashCode(), dvalues);
                }
            }

        } finally {
            if(implicitTx) {
                closeTransaction();
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
        queryCache.remove(createCacheKey(null, null, null, null, false).longHashCode());
        queryCache.remove(createCacheKey(null,null,null,null,true).longHashCode());

        queryCache.remove(createCacheKey(null,null,null,defaultContext,false).longHashCode());
        queryCache.remove(createCacheKey(null,null,null,defaultContext,true).longHashCode());


        // remove all possible combinations of this triple as they may appear in the cache
        queryCache.remove(createCacheKey(subject,null,null,null,false).longHashCode());
        queryCache.remove(createCacheKey(subject,null,null,null,true).longHashCode());
        queryCache.remove(createCacheKey(null,predicate,null,null,false).longHashCode());
        queryCache.remove(createCacheKey(null,predicate,null,null,true).longHashCode());
        queryCache.remove(createCacheKey(null,null,object,null,false).longHashCode());
        queryCache.remove(createCacheKey(null,null,object,null,true).longHashCode());

        queryCache.remove(createCacheKey(subject,predicate,null,null,false).longHashCode());
        queryCache.remove(createCacheKey(subject,predicate,null,null,true).longHashCode());
        queryCache.remove(createCacheKey(subject,null,object,null,false).longHashCode());
        queryCache.remove(createCacheKey(subject,null,object,null,true).longHashCode());
        queryCache.remove(createCacheKey(null,predicate,object,null,false).longHashCode());
        queryCache.remove(createCacheKey(null,predicate,object,null,true).longHashCode());


        queryCache.remove(createCacheKey(subject,predicate,object,null,false).longHashCode());
        queryCache.remove(createCacheKey(subject,predicate,object,null,true).longHashCode());

        for(Resource context : contexts) {
            queryCache.remove(createCacheKey(null,null,null,context,false).longHashCode());
            queryCache.remove(createCacheKey(null,null,null,context,true).longHashCode());
            queryCache.remove(createCacheKey(subject,null,null,context,false).longHashCode());
            queryCache.remove(createCacheKey(subject,null,null,context,true).longHashCode());
            queryCache.remove(createCacheKey(null,predicate,null,context,false).longHashCode());
            queryCache.remove(createCacheKey(null,predicate,null,context,true).longHashCode());
            queryCache.remove(createCacheKey(null,null,object,context,false).longHashCode());
            queryCache.remove(createCacheKey(null,null,object,context,true).longHashCode());

            queryCache.remove(createCacheKey(subject,predicate,null,context,false).longHashCode());
            queryCache.remove(createCacheKey(subject,predicate,null,context,true).longHashCode());
            queryCache.remove(createCacheKey(subject,null,object,context,false).longHashCode());
            queryCache.remove(createCacheKey(subject,null,object,context,true).longHashCode());
            queryCache.remove(createCacheKey(null,predicate,object,context,false).longHashCode());
            queryCache.remove(createCacheKey(null,predicate,object,context,true).longHashCode());

            queryCache.remove(createCacheKey(subject,predicate,object,context,false).longHashCode());
            queryCache.remove(createCacheKey(subject,predicate,object,context,true).longHashCode());
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


    /**
     * Get the root sail in the wrapped sail stack
     * @param sail
     * @return
     */
    private KiWiConnection getKiWiConnection(SailConnection sail) {
        if(sail instanceof KiWiSailConnection) {
            return ((KiWiSailConnection) sail).getDatabaseConnection();
        } else if(sail instanceof SailConnectionWrapper) {
            return getKiWiConnection(((SailConnectionWrapper) sail).getWrappedConnection());
        } else {
            throw new IllegalArgumentException("root sail connection is not a KiWiSailConnection or could not be found");
        }
    }

    private class IDTripleLoader implements Function<Long,Statement> {
        @Override
        public Statement apply(Long input) {
            try {
                return kiWiConnection.loadTripleById(input);
            } catch (SQLException e) {
                log.error("could not load triple with ID {}", input);
                throw new RuntimeException(e);
            }
        }
    }

    private class IDTripleExtractor implements Function<Statement,Long> {
        @Override
        public Long apply(Statement input) {
            if(input instanceof KiWiTriple) {
                return ((KiWiTriple) input).getId();
            } else {
                return -1L;
            }
        }
    }
}
