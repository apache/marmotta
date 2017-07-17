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
package org.apache.marmotta.commons.sesame.contextaware;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.SingletonIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.UpdateContext;
import org.eclipse.rdf4j.sail.helpers.SailConnectionWrapper;

/**
 * A SAIL connection overriding all provided context values with the default context used when creating the connection
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContextAwareSailConnection extends SailConnectionWrapper {

    private final Resource context;

    public ContextAwareSailConnection(SailConnection wrappedCon, Resource context) {
        super(wrappedCon);
        this.context = context;
    }

    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred, Value obj, boolean includeInferred, Resource... contexts) throws SailException {
        return super.getStatements(subj, pred, obj, includeInferred, context);
    }

    @Override
    public void addStatement(Resource subj, IRI pred, Value obj, Resource... contexts) throws SailException {
        super.addStatement(subj, pred, obj, context);
    }

    @Override
    public void addStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource... contexts) throws SailException {
        super.addStatement(modify, subj, pred, obj, context);
    }

    @Override
    public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
        return new SingletonIteration<>(context);
    }

    @Override
    public void removeStatements(Resource subj, IRI pred, Value obj, Resource... contexts) throws SailException {
        super.removeStatements(subj, pred, obj, context);
    }

    @Override
    public void removeStatement(UpdateContext modify, Resource subj, IRI pred, Value obj, Resource... contexts) throws SailException {
        super.removeStatement(modify, subj, pred, obj, context);
    }

    @Override
    public void clear(Resource... contexts) throws SailException {
        super.clear(context);
    }
}
