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

import java.util.ArrayList;
import java.util.List;

/**
* Find the offset and limit values in a tuple expression
*
* @author Sebastian Schaffert (sschaffert@apache.org)
*/
public class OrderFinder extends QueryModelVisitorBase<RuntimeException> {

    private List<OrderElem> elements = new ArrayList<>();

    private OrderFinder(TupleExpr expr) {
        expr.visit(this);
    }

    public static List<OrderElem> find(TupleExpr expr) {
        return new OrderFinder(expr).elements;
    }

    @Override
    public void meet(Order node) throws RuntimeException {
        elements.addAll(node.getElements());
    }



    @Override
    public void meet(Projection node) throws RuntimeException {
        // stop at projection, subquery
    }

    @Override
    public void meet(Union node) throws RuntimeException {
        // stop at projection, subquery
    }

}
