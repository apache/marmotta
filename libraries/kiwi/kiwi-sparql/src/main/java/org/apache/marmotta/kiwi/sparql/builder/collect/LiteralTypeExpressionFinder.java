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

package org.apache.marmotta.kiwi.sparql.builder.collect;

import org.apache.marmotta.kiwi.sparql.builder.ValueType;
import org.apache.marmotta.kiwi.sparql.function.NativeFunction;
import org.apache.marmotta.kiwi.sparql.function.NativeFunctionRegistry;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Functions that return a string literal do so with the string literal of the same kind as the first
 * argument (simple literal, plain literal with same language tag, xsd:string). This visitor
 * tries finding the relevant subexpression in a complex value expression.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LiteralTypeExpressionFinder  extends QueryModelVisitorBase<RuntimeException> {

    public Var expr = null;

    public LiteralTypeExpressionFinder(ValueExpr expr) {
        expr.visit(this);
    }

    @Override
    public void meet(FunctionCall node) throws RuntimeException {
        NativeFunction nf = NativeFunctionRegistry.getInstance().get(node.getURI());
        if(node.getArgs().size() > 0 && nf.getReturnType() == ValueType.STRING) {
            node.getArgs().get(0).visit(this);
        }
        // otherwise stop here, the function call hides the type and language anyways
    }

    @Override
    public void meet(Var node) throws RuntimeException {
        expr = node;
    }

    /**
     * Method called by all of the other <tt>meet</tt> methods that are not
     * overridden in subclasses. This method can be overridden in subclasses to
     * define default behaviour when visiting nodes. The default behaviour of
     * this method is to visit the node's children.
     *
     * @param node The node that is being visited.
     */
    @Override
    protected void meetNode(QueryModelNode node) throws RuntimeException {
    }
}
