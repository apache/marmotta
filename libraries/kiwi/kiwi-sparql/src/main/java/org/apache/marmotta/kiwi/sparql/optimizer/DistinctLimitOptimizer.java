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

package org.apache.marmotta.kiwi.sparql.optimizer;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Push limits and distincts down to inside the projection, because then we can potentially translate them into a database
 * LIMIT for more efficient querying
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class DistinctLimitOptimizer implements QueryOptimizer {

    private static Logger log = LoggerFactory.getLogger(DistinctLimitOptimizer.class);

    @Override
    public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
        //if( new LimitPreconditions(tupleExpr).isAllowed() ) {
            log.debug("applying distinct/limit optimizations ...");

            tupleExpr.visit(new LimitRelocator());
            tupleExpr.visit(new DistinctRelocator());
            tupleExpr.visit(new ReducedRelocator());
        //}
    }

    /**
     * We can only safely push down a distinct or limit to the outermost JOIN, Filter, or Statement Pattern in case the
     * query does not contain an ORDER BY or one of the other constructs that affect the number of results
     */
    private static class LimitPreconditions extends QueryModelVisitorBase<RuntimeException> {

        private boolean allowed = true;

        private LimitPreconditions(TupleExpr expr) {
            expr.visit(this);
        }

        @Override
        public void meet(Order node) throws RuntimeException {
            super.meet(node);

            allowed = false;
        }

        @Override
        public void meet(Group node) throws RuntimeException {
            super.meet(node);

            allowed = false;
        }

        @Override
        public void meet(LeftJoin node) throws RuntimeException {
            super.meet(node);

            allowed = false;
        }


        @Override
        public void meet(Union node) throws RuntimeException {
            super.meet(node);

            allowed = false;
        }


        @Override
        public void meet(Filter node) throws RuntimeException {
            // break traversal
        }

        @Override
        public void meet(Join node) throws RuntimeException {
            // break traversal
        }

        public boolean isAllowed() {
            return allowed;
        }
    }

    /**
     * Move down the Slide node so its parent its children are the Join, Filter or Statement pattern nodes.
     */
    private static class LimitRelocator extends QueryModelVisitorBase<RuntimeException> {

        private LimitRelocator() {
        }

        @Override
        public void meet(Slice node) throws RuntimeException {
            TupleExpr child = node.getArg();

            if(!isSupported(child) && child instanceof UnaryTupleOperator) {
                UnaryTupleOperator replacement = (UnaryTupleOperator)child.clone();

                // switch positions of child and node
                node.replaceWith(replacement);
                node.setArg(((UnaryTupleOperator) child).getArg().clone());
                replacement.setArg(node.clone());

                // visit the newly inserted replacement node (i.e. the clone of child now containing the old "node" as
                // child, so "node" can be bubbled down further if needed)
                replacement.visit(this);
            }

        }

        private static boolean isSupported(TupleExpr expr) {
            if(expr instanceof Join) {
                return true;
            } else if(expr instanceof LeftJoin) {
                return true;
            } else if(expr instanceof Filter) {
                return true;
            } else if(expr instanceof StatementPattern) {
                return true;
            } else if(expr instanceof Union) {
                return true;
            } else if(expr instanceof Order) {
                return true;
            } else if(expr instanceof Group) {
                return true;
            } else {
                return false;
            }
        }
    }

    private static class DistinctRelocator extends QueryModelVisitorBase<RuntimeException> {

        private DistinctRelocator() {
        }

        @Override
        public void meet(Distinct node) throws RuntimeException {
            TupleExpr child = node.getArg();

            if(!isSupported(child) && child instanceof UnaryTupleOperator) {
                UnaryTupleOperator replacement = (UnaryTupleOperator)child.clone();

                // switch positions of child and node
                node.replaceWith(replacement);
                node.setArg(((UnaryTupleOperator) child).getArg().clone());
                replacement.setArg(node.clone());

                // visit the newly inserted replacement node (i.e. the clone of child now containing the old "node" as
                // child, so "node" can be bubbled down further if needed)
                replacement.visit(this);
            }

        }


        private static boolean isSupported(TupleExpr expr) {
            if(expr instanceof Join) {
                return true;
            } else if(expr instanceof LeftJoin) {
                return true;
            } else if(expr instanceof Filter) {
                return true;
            } else if(expr instanceof StatementPattern) {
                return true;
            } else if(expr instanceof Union) {
                return true;
            } else if(expr instanceof Order) {
                return true;
            } else if(expr instanceof Group) {
                return true;
            } else if(expr instanceof Slice) {
                return true;
            } else {
                return false;
            }
        }
    }

    private static class ReducedRelocator extends QueryModelVisitorBase<RuntimeException> {

        private ReducedRelocator() {
        }

        @Override
        public void meet(Reduced node) throws RuntimeException {
            TupleExpr child = node.getArg();

            if(!isSupported(child) && child instanceof UnaryTupleOperator) {
                UnaryTupleOperator replacement = (UnaryTupleOperator)child.clone();

                // switch positions of child and node
                node.replaceWith(replacement);
                node.setArg(((UnaryTupleOperator) child).getArg().clone());
                replacement.setArg(node.clone());

                // visit the newly inserted replacement node (i.e. the clone of child now containing the old "node" as
                // child, so "node" can be bubbled down further if needed)
                replacement.visit(this);
            }

        }


        private static boolean isSupported(TupleExpr expr) {
            if(expr instanceof Join) {
                return true;
            } else if(expr instanceof LeftJoin) {
                return true;
            } else if(expr instanceof Filter) {
                return true;
            } else if(expr instanceof StatementPattern) {
                return true;
            } else if(expr instanceof Union) {
                return true;
            } else if(expr instanceof Order) {
                return true;
            } else if(expr instanceof Group) {
                return true;
            } else if(expr instanceof Slice) {
                return true;
            } else if(expr instanceof Distinct) {
                return true;
            } else if(expr instanceof Reduced) {
                return true;
            } else {
                return false;
            }
        }
    }



}
