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
package org.apache.marmotta.kiwi.sparql.optimizer;

import org.apache.marmotta.kiwi.sparql.function.NativeFunctionRegistry;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.VarNameCollector;

import java.util.Set;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 23.11.15.
 */
public class NativeFilterOptimizer implements QueryOptimizer {

    @Override
    public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
        FilterFinder f = new FilterFinder(tupleExpr);
        tupleExpr.visit(f);
    }

	/*--------------------------*
	 * Inner class FilterFinder *
	 *--------------------------*/

    protected static class FilterFinder extends QueryModelVisitorBase<RuntimeException> {

        protected final TupleExpr tupleExpr;

        public FilterFinder(TupleExpr tupleExpr) {
            this.tupleExpr = tupleExpr;
        }

        @Override
        public void meet(Filter filter) {
            super.meet(filter);
            if(filter.getCondition() instanceof FunctionCall) {
                String uri = ((FunctionCall) filter.getCondition()).getURI();
                if(NativeFunctionRegistry.getInstance().get(uri) != null) {
                    FilterRelocator.relocate(filter);
                }
            } else {
                FilterRelocator.relocate(filter);
            }
        }
    }


	/*-----------------------------*
	 * Inner class FilterRelocator *
	 *-----------------------------*/

    protected static class FilterRelocator extends QueryModelVisitorBase<RuntimeException> {

        public static void relocate(Filter filter) {
            filter.visit(new FilterRelocator(filter));
        }

        protected final Filter filter;

        protected final Set<String> filterVars;

        public FilterRelocator(Filter filter) {
            this.filter = filter;
            filterVars = VarNameCollector.process(filter.getCondition());
        }

        @Override
        protected void meetNode(QueryModelNode node) {
            // By default, do not traverse
            assert node instanceof TupleExpr;
            relocate(filter, (TupleExpr) node);
        }

        @Override
        public void meet(Join join) {
            if (join.getLeftArg().getBindingNames().containsAll(filterVars)) {
                // All required vars are bound by the left expr
                join.getLeftArg().visit(this);
            }
            else if (join.getRightArg().getBindingNames().containsAll(filterVars)) {
                // All required vars are bound by the right expr
                join.getRightArg().visit(this);
            }
            else {
                relocate(filter, join);
            }
        }

        @Override
        public void meet(LeftJoin leftJoin) {
            if (leftJoin.getLeftArg().getBindingNames().containsAll(filterVars)) {
                leftJoin.getLeftArg().visit(this);
            }
            else {
                relocate(filter, leftJoin);
            }
        }

        @Override
        public void meet(Union union) {
            Filter clone = new Filter();
            clone.setCondition(filter.getCondition().clone());

            relocate(filter, union.getLeftArg());
            relocate(clone, union.getRightArg());

            FilterRelocator.relocate(filter);
            FilterRelocator.relocate(clone);
        }

        @Override
        public void meet(Difference node) {
            Filter clone = new Filter();
            clone.setCondition(filter.getCondition().clone());

            relocate(filter, node.getLeftArg());
            relocate(clone, node.getRightArg());

            FilterRelocator.relocate(filter);
            FilterRelocator.relocate(clone);
        }

        @Override
        public void meet(Intersection node) {
            Filter clone = new Filter();
            clone.setCondition(filter.getCondition().clone());

            relocate(filter, node.getLeftArg());
            relocate(clone, node.getRightArg());

            FilterRelocator.relocate(filter);
            FilterRelocator.relocate(clone);
        }

        @Override
        public void meet(Extension node) {
            if (node.getArg().getBindingNames().containsAll(filterVars)) {
                node.getArg().visit(this);
            }
            else {
                relocate(filter, node);
            }
        }

        @Override
        public void meet(EmptySet node) {
            if (filter.getParentNode() != null) {
                // Remove filter from its original location
                filter.replaceWith(filter.getArg());
            }
        }

        @Override
        public void meet(Filter filter) {
            // Filters are commutative
            filter.getArg().visit(this);
        }

        @Override
        public void meet(Distinct node) {
            node.getArg().visit(this);
        }

        @Override
        public void meet(Order node) {
            node.getArg().visit(this);
        }

        @Override
        public void meet(QueryRoot node) {
            node.getArg().visit(this);
        }

        @Override
        public void meet(Reduced node) {
            node.getArg().visit(this);
        }

        protected void relocate(Filter filter, TupleExpr newFilterArg) {
            if (filter.getArg() != newFilterArg) {
                if (filter.getParentNode() != null) {
                    // Remove filter from its original location
                    filter.replaceWith(filter.getArg());
                }

                // Insert filter at the new location
                newFilterArg.replaceWith(filter);
                filter.setArg(newFilterArg);
            }
        }
    }

}
