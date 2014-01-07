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
import com.google.common.base.Function;
import com.google.common.base.Objects;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.function.Equality;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.util.Map;
import java.util.Set;

/**
 * Provide some utility functions for managing statements (e.g. different forms of equivalence)
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class StatementCommons {

    private final static Equivalence<Statement> TRIPLE_EQUIVALENCE = new Equivalence<Statement>() {
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


    private final static Equivalence<Statement> QUADRUPLE_EQUIVALENCE = new Equivalence<Statement>() {
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

    /**
     * Return triple equivalence, taking only into account subject, predicate and object
     *
     * @return
     */
    public static Equivalence<Statement> tripleEquivalence() {
        return TRIPLE_EQUIVALENCE;
    }

    /**
     * Return quadruple equivalence, taking into account subject, predicate, object, and context.
     *
     * @return
     */
    public static Equivalence<Statement> quadrupleEquivalence() {
        return QUADRUPLE_EQUIVALENCE;

    }

    /**
     * Create a new set for statements using the triple equivalence (based on subject, predicate, object only)
     * @param <T>
     * @return
     */
    public static <T extends Statement> Set<T> newTripleSet() {
        //return new EquivalenceHashSet<>(tripleEquivalence());
        return new FastSet<>(equivalenceEquality(tripleEquivalence()));
    }

    /**
     * Create a new set for statements using the triple equivalence (based on subject, predicate, object and context)
     * @param <T>
     * @return
     */
    public static <T extends Statement> Set<T> newQuadrupleSet() {
        //return new EquivalenceHashSet<>(quadrupleEquivalence());
        return new FastSet<>(equivalenceEquality(quadrupleEquivalence()));
    }

    /**
     * Create a new map where the keys are statements and the equivalence relation used for keys is triple
     * equivalence (based on subject, predicate, object only)
     *
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K extends Statement, V> Map<K,V> newTripleMap() {
        //return new EquivalenceHashMap<>(tripleEquivalence());
        return new FastMap<>(equivalenceEquality(tripleEquivalence()));
    }

    /**
     * Create a new map where the keys are statements and the equivalence relation used for keys is triple
     * equivalence (based on subject, predicate, object only)
     *
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K extends Statement, V> Map<K,V> newQuadrupleMap() {
        //return new EquivalenceHashMap<>(quadrupleEquivalence());
        return new FastMap<>(equivalenceEquality(quadrupleEquivalence()));
    }



    private static <E> Equality<E> equivalenceEquality(final Equivalence<E> equivalence) {
        return new Equality<E>() {
            private static final long serialVersionUID = 1L;

            @Override
            public int hashCodeOf(E object) {
                return equivalence.hash(object);
            }

            @Override
            public boolean areEqual(E left, E right) {
                return equivalence.equivalent(left, right);
            }

            @Override
            public int compare(E left, E right) {
                return equivalence.hash(left) - equivalence.hash(right);
            }

            @Override
            public int hashCode() {
                return equivalence.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return obj.hashCode() == hashCode();
            }
        };
    }

    public static class TripleEquality implements Function<Statement,Statement> {

        @Override
        public Statement apply(final Statement input) {
            return new Statement() {
                @Override
                public Resource getSubject() {
                    return input.getSubject();
                }

                @Override
                public URI getPredicate() {
                    return input.getPredicate();
                }

                @Override
                public Value getObject() {
                    return input.getObject();
                }

                @Override
                public Resource getContext() {
                    return input.getContext();
                }

                @Override
                public int hashCode() {
                    return TRIPLE_EQUIVALENCE.hash(input);
                }

                @Override
                public boolean equals(Object obj) {
                    return TRIPLE_EQUIVALENCE.equivalent(input, (Statement) obj);
                }

                @Override
                public String toString() {
                    return input.toString();
                }
            };
        }
    }
}
