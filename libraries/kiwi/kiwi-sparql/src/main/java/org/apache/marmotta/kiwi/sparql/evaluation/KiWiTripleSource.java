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

package org.apache.marmotta.kiwi.sparql.evaluation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import org.apache.marmotta.kiwi.sparql.sail.KiWiSparqlSailConnection;
import org.openrdf.model.*;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.sail.SailException;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;

/**
 * TripleSource implementation based on a KiWi triple store.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiTripleSource implements TripleSource {
    private ValueFactory factory;
    private boolean inferred;
    private KiWiSparqlSailConnection connection;

    public KiWiTripleSource(KiWiSparqlSailConnection connection, ValueFactory factory, boolean inferred) {
        this.factory = factory;
        this.inferred   = inferred;
        this.connection = connection;
    }

    /**
     * Gets all statements that have a specific subject, predicate and/or object.
     * All three parameters may be null to indicate wildcards. Optionally a (set
     * of) context(s) may be specified in which case the result will be
     * restricted to statements matching one or more of the specified contexts.
     *
     * @param subj     A Resource specifying the subject, or <tt>null</tt> for a
     *                 wildcard.
     * @param pred     A URI specifying the predicate, or <tt>null</tt> for a wildcard.
     * @param obj      A Value specifying the object, or <tt>null</tt> for a wildcard.
     * @param contexts The context(s) to get the statements from. Note that this parameter
     *                 is a vararg and as such is optional. If no contexts are supplied
     *                 the method operates on the entire repository.
     * @return An iterator over the relevant statements.
     * @throws org.openrdf.query.QueryEvaluationException
     *          If the triple source failed to get the statements.
     */
    @Override
    public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj, URI pred, Value obj, Resource... contexts) throws QueryEvaluationException {
        try {
            return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(
                    connection.getStatements(subj, pred, obj, inferred, contexts)
            ) {
                @Override
                protected QueryEvaluationException convert(Exception e) {
                    if (e instanceof ClosedByInterruptException) {
                        return new QueryInterruptedException(e);
                    }
                    else if (e instanceof IOException) {
                        return new QueryEvaluationException(e);
                    }
                    else if (e instanceof RuntimeException) {
                        throw (RuntimeException)e;
                    }
                    else if (e == null) {
                        throw new IllegalArgumentException("e must not be null");
                    }
                    else {
                        throw new IllegalArgumentException("Unexpected exception type: " + e.getClass());
                    }
                }
            };
        } catch (SailException ex) {
            throw new QueryEvaluationException(ex);
        }
    }

    /**
     * Gets a ValueFactory object that can be used to create URI-, blank node-
     * and literal objects.
     *
     * @return a ValueFactory object for this TripleSource.
     */
    @Override
    public ValueFactory getValueFactory() {
        return factory;
    }

}
