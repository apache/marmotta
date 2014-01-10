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
package org.apache.marmotta.ldcache.sail;

import info.aduna.iteration.CloseableIteration;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.ldcache.services.LDCache;
import org.openrdf.model.*;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiLinkedDataSailConnection extends NotifyingSailConnectionWrapper {

    private LDCache ldcache;

    private SesameFilter<Resource> acceptor;

    public KiWiLinkedDataSailConnection(NotifyingSailConnection wrappedCon, LDCache ldcache, SesameFilter<Resource> acceptor) {
        super(wrappedCon);

        this.ldcache = ldcache;
        this.acceptor = acceptor;
    }


    /**
     * Handle a triple store query event. This method carries out the following steps:
     * 1. if the subject of the query is local, return immediately without doing anything
     * 2. if the subject of the query is a remote resource, call refreshResource on it; refreshed triples will
     *    be part of the currently active transaction and thus be available to the listTriples method execution
     * 3, if the subject of the query is null, refresh all non-local resources in the triple store
     */
    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts) throws SailException {
        if(subj != null && isAcceptable(subj)) {
            ldcache.refresh((URI) subj);
        }

        // the refreshed resources will anyways be stored in the same triple store, so we can simply delegate the query
        // to the wrapped repository

        return super.getStatements(subj, pred, obj, includeInferred, contexts);
    }


    private boolean isAcceptable(Resource resource) {
        if(resource instanceof BNode) {
            return false;
        }
        return acceptor.accept(resource);
    }
}
