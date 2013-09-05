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

package org.apache.marmotta.commons.sesame.model;

import com.google.common.base.Equivalence;
import com.google.common.base.Objects;
import org.openrdf.model.Statement;

/**
 * Provide some utility functions for managing statements (e.g. different forms of equivalence)
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class StatementCommons {

    /**
     * Return triple equivalence, taking only into account subject, predicate and object
     *
     * @return
     */
    public static Equivalence<Statement> tripleEquivalence() {
        return new Equivalence<Statement>() {
            @Override
            protected boolean doEquivalent(Statement a, Statement b) {
                if(a == b) return true;

                if(!Objects.equal(a.getSubject(), b.getSubject())) return false;
                if(!Objects.equal(a.getPredicate(), b.getPredicate())) return false;
                if(!Objects.equal(a.getObject(), b.getObject())) return false;
                return true;
            }

            @Override
            protected int doHash(Statement statement) {
                return Objects.hashCode(statement.getSubject(), statement.getPredicate(), statement.getObject());
            }

            @Override
            public boolean equals(Object obj) {
                return this.getClass().equals(obj.getClass());
            }

            @Override
            public int hashCode() {
                return this.getClass().hashCode();
            }
        };
    }

    /**
     * Return quadruple equivalence, taking into account subject, predicate, object, and context.
     *
     * @return
     */
    public static Equivalence<Statement> quadrupleEquivalence() {
        return new Equivalence<Statement>() {
            @Override
            protected boolean doEquivalent(Statement a, Statement b) {
                if(a == b) return true;

                if(!Objects.equal(a.getSubject(), b.getSubject())) return false;
                if(!Objects.equal(a.getPredicate(), b.getPredicate())) return false;
                if(!Objects.equal(a.getObject(), b.getObject())) return false;
                if(!Objects.equal(a.getContext(), b.getContext())) return false;
                return true;
            }

            @Override
            protected int doHash(Statement statement) {
                return Objects.hashCode(statement.getSubject(), statement.getPredicate(), statement.getObject(), statement.getContext());
            }

            @Override
            public boolean equals(Object obj) {
                return this.getClass().equals(obj.getClass());
            }

            @Override
            public int hashCode() {
                return this.getClass().hashCode();
            }
        };

    }

}
