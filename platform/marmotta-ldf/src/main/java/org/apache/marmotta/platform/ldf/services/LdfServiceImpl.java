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
package org.apache.marmotta.platform.ldf.services;

import org.apache.marmotta.commons.sesame.repository.ResultUtils;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldf.api.LdfService;
import org.apache.marmotta.platform.ldf.sesame.LdfRDFHandler;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.*;

import javax.inject.Inject;
import java.io.OutputStream;

/**
 * Linked Media Fragments service implementation
 *
 * @author Sergio Fernández
 */
public class LdfServiceImpl implements LdfService {

    @Inject
    private SesameService sesameService;

    @Override
    public void writeFragment(URI subject, URI predicate, Value object, int offset, int limit, RDFFormat format, OutputStream out) throws RepositoryException {
        writeFragment(subject, predicate, object, null, offset, limit, format, out);
    }

    @Override
    public void writeFragment(URI subject, URI predicate, Value object, Resource context, int offset, int limit, RDFFormat format, OutputStream out) throws RepositoryException {
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();
            RepositoryResult<Statement> statements = conn.getStatements(subject, predicate, object, true, context);
            RDFHandler handler = new LdfRDFHandler(Rio.createWriter(format, out), context, offset, limit);
            Rio.write(ResultUtils.iterable(statements), handler);
        } catch (RDFHandlerException e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }
    }
}
