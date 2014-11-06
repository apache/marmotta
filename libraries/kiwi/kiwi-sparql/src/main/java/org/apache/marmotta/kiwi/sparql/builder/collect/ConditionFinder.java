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

/**
 * Check if a variable is used as a condition somewhere and therefore needs to be resolved.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ConditionFinder extends QueryModelVisitorBase<RuntimeException> {

    public boolean found = false;

    private String varName;
    private boolean valueNeeded = false; // indicate if the value of the node is actually needed or the id is sufficient

    public ConditionFinder(String varName, TupleExpr expr) {
        this.varName = varName;

        expr.visit(this);
    }

    @Override
    public void meet(Var node) throws RuntimeException {
        if(valueNeeded && !found) {
            found = node.getName().equals(varName);
        }
    }

    @Override
    public void meet(Count node) throws RuntimeException {
        if(!found && node.getArg() == null) {
            // special case: count(*), we need the variable
            found = true;
        } else {
            super.meet(node);
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
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(BNodeGenerator node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Compare node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Datatype node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(IRIFunction node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(IsBNode node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(IsLiteral node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(IsNumeric node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(IsResource node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(IsURI node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Label node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Lang node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(LangMatches node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Like node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(MathExpr node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(LocalName node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Max node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Min node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Namespace node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Regex node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(SameTerm node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Str node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(Sum node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(FunctionCall node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(OrderElem node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

    @Override
    public void meet(GroupElem node) throws RuntimeException {
        valueNeeded = true;
        super.meet(node);
    }

}
