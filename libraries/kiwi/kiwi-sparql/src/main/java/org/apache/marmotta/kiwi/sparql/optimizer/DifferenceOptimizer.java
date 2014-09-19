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

import com.google.common.collect.Sets;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This optimizer replaces occurrences of Difference with constructs that can be translated to SQL more easily:
 * - in case the left and right argument have shared variables, the right argument is replaced by a Filter with a NOT EXISTS subquery
 * - in case the left and right argument have no shared variables, the right argument is ignored
 *
 * Note that this transformation does not respect the case 3 documented in the SPARQL standard regarding variable scoping
 * of inner FILTERs (http://www.w3.org/TR/sparql11-query/#neg-notexists-minus). This is a border case and almost impossible to
 * translate to SQL with its unification semantics.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class DifferenceOptimizer implements QueryOptimizer {

    private static Logger log = LoggerFactory.getLogger(DifferenceOptimizer.class);

    @Override
    public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
        tupleExpr.visit(new DifferenceReplacer());
    }


    private static class DifferenceReplacer extends QueryModelVisitorBase<RuntimeException> {

        @Override
        public void meet(Difference node) throws RuntimeException {
            Set<String> leftVars  = new VariableFinder(node.getLeftArg()).variables;
            Set<String> rightVars = new VariableFinder(node.getRightArg()).variables;

            if(Sets.intersection(leftVars,rightVars).size() > 0) {
                // left and right share variables, so we replace with a FILTER with NOT EXISTS
                log.debug("replacing SPARQL MINUS with a FILTER on NOT EXISTS, because there are shared variables");
                log.debug("AST before:\n{}", node);

                ValueExpr notExists = new Not(new Exists(node.getRightArg()));
                Filter replacement = new Filter(node.getLeftArg(), notExists);
                node.replaceWith(replacement);

                log.debug("AST after:\n{}", node);
            } else {
                // left and right do not share variables, so we replace with the left subquery
                log.debug("replacing SPARQL MINUS with its left argument, because there are no variables");
                node.replaceWith(node.getLeftArg());
            }
        }
    }


    private static class VariableFinder extends QueryModelVisitorBase<RuntimeException> {
        Set<String> variables = new HashSet<>();

        public VariableFinder(TupleExpr expr) {
            expr.visit(this);
        }

        @Override
        public void meet(Var node) throws RuntimeException {
            if(!node.isAnonymous() && !node.isConstant()) {
                variables.add(node.getName());
            }
        }
    }
}
