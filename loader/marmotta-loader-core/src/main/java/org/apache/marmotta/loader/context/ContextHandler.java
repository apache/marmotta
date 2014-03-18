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
package org.apache.marmotta.loader.context;

import org.apache.marmotta.loader.api.LoaderHandler;
import org.apache.marmotta.loader.wrapper.LoaderHandlerWrapper;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.rio.RDFHandlerException;

/**
 * A handler adding a pre-defined context to each statement
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContextHandler extends LoaderHandlerWrapper {

    private URI context;

    public ContextHandler(LoaderHandler handler, URI context) {
        super(handler);
        this.context = context;
    }


    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        Statement wrapped = new ContextStatementImpl(st.getSubject(),st.getPredicate(),st.getObject(), context);

        super.handleStatement(wrapped);
    }
}
