/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.kiwi.contextaware;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.SingletonIteration;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

/**
 * A SAIL connection overriding all provided context values with the default context used when creating the connection
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContextAwareSailConnection extends SailConnectionWrapper {

    private Resource context;

    public ContextAwareSailConnection(SailConnection wrappedCon, Resource context) {
        super(wrappedCon);
        this.context = context;
    }

    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts) throws SailException {
        return super.getStatements(subj, pred, obj, includeInferred, context);
    }

    @Override
    public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        super.addStatement(subj, pred, obj, context);
    }

    @Override
    public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
        return new SingletonIteration<Resource, SailException>(context);
    }

    @Override
    public void clear(Resource... contexts) throws SailException {
        super.clear(context);
    }
}
