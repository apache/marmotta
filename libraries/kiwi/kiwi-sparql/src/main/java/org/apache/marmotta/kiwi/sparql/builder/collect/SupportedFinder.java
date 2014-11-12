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

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.sparql.function.NativeFunctionRegistry;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Check if all constructs in the query are supported natively. Whenever you add a new construct to SQLBuilder
 * or ValueExpressionEvaluator, it should be removed here.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SupportedFinder extends QueryModelVisitorBase<RuntimeException> {

    private boolean supported = true;
    private KiWiDialect dialect;

    public SupportedFinder(TupleExpr expr, KiWiDialect dialect) {
        this.dialect = dialect;

        expr.visit(this);
    }

    public SupportedFinder(ValueExpr expr, KiWiDialect dialect) {
        this.dialect = dialect;

        expr.visit(this);
    }

    public boolean isSupported() {
        return supported;
    }


    @Override
    public void meet(ArbitraryLengthPath node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(BindingSetAssignment node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(CompareAll node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(CompareAny node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(Count node) throws RuntimeException {
        if(!dialect.isArraySupported()) {
            supported = false;
        } else {
            super.meet(node);
        }
    }


    @Override
    public void meet(Datatype node) throws RuntimeException {
        // datatype checking would require a self-join of the nodes table for variables and some other magic for
        // generated values, so it is currently not yet supported
        supported = false;
    }


    @Override
    public void meet(DescribeOperator node) throws RuntimeException {
        supported = false;
    }


    @Override
    public void meet(Difference node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(EmptySet node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(FunctionCall node) throws RuntimeException {
        if(!isFunctionSupported(node)) {
            supported = false;
        } else {
            super.meet(node);
        }
    }

    @Override
    public void meet(Intersection node) throws RuntimeException {
        supported = false;
    }



    @Override
    public void meet(MultiProjection node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(Sample node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(Service node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(ZeroLengthPath node) throws RuntimeException {
        supported = false;
    }

    @Override
    public void meet(ListMemberOperator node) throws RuntimeException {
        supported = false;
    }

    /**
     * All update expressions are not directly supported; however, their query parts should work fine!
     */
    @Override
    protected void meetUpdateExpr(UpdateExpr node) throws RuntimeException {
        supported = false;
    }

    private boolean isFunctionSupported(FunctionCall fc) {
        return NativeFunctionRegistry.getInstance().get(fc.getURI()) != null && NativeFunctionRegistry.getInstance().get(fc.getURI()).isSupported(dialect);
    }


    private static boolean isAtomic(ValueExpr expr) {
        return expr instanceof Var || expr instanceof ValueConstant;
    }

    private static boolean isConstant(ValueExpr expr) {
        return expr instanceof ValueConstant;
    }

}
