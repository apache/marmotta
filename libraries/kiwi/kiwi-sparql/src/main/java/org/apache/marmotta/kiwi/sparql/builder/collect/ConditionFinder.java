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

import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import java.util.HashSet;
import java.util.Set;

/**
 * Check if a variable is used as a condition somewhere and therefore needs to be resolved.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ConditionFinder extends QueryModelVisitorBase<RuntimeException> {

    // indicate (if > 0) if the value of variables in recursive calls need to be retrieved because the
    // enclosing construct operates on values instead of nodes
    int valueNeeded = 0;

    // set of variables that need a value to be resolved (used by ExtensionElem resolution)
    public Set<String> neededVariables = new HashSet<>();

    public ConditionFinder(TupleExpr expr) {
        expr.visit(this);
    }

    @Override
    public void meet(Var node) throws RuntimeException {
        if(valueNeeded > 0) {
            neededVariables.add(node.getName());
        }
    }

    @Override
    public void meet(StatementPattern node) throws RuntimeException {
        // stop, no condition
    }


    @Override
    public void meet(Union node) throws RuntimeException {
        // stop, subquery
    }

    @Override
    public void meet(Projection node) throws RuntimeException {
        // stop, subquery
    }


    // the following constructs all need the value, so set the flag

    @Override
    public void meet(Avg node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(BNodeGenerator node) throws RuntimeException {
        valueNeeded++;
        if(node.getNodeIdExpr() != null) {
            node.getNodeIdExpr().visit(this);
        }
        valueNeeded--;
    }

    @Override
    public void meet(Compare node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Datatype node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(IRIFunction node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(IsBNode node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(IsLiteral node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(IsNumeric node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(IsResource node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(IsURI node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Label node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Lang node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(LangMatches node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Like node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(MathExpr node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(LocalName node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Max node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Min node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Namespace node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Regex node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(SameTerm node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Str node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(Sum node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(FunctionCall node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(OrderElem node) throws RuntimeException {
        valueNeeded++;
        node.getExpr().visit(this);
        valueNeeded--;
    }

    @Override
    public void meet(GroupElem node) throws RuntimeException {
        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

    @Override
    public void meet(ExtensionElem node) throws RuntimeException {
        neededVariables.add(node.getName());

        valueNeeded++;
        super.meet(node);
        valueNeeded--;
    }

}
