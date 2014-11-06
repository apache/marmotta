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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
* Find the offset and limit values in a tuple expression
*
* @author Sebastian Schaffert (sschaffert@apache.org)
*/
public class ExtensionFinder extends QueryModelVisitorBase<RuntimeException> {

    private static Logger log = LoggerFactory.getLogger(ExtensionFinder.class);

    public List<ExtensionElem> elements = new ArrayList<>();

    public ExtensionFinder(TupleExpr expr) {
        expr.visit(this);
    }

    @Override
    public void meet(Extension node) throws RuntimeException {
        // visit children before, as there might be dependencies
        super.meet(node);

        for(ExtensionElem elem : node.getElements()) {
            if(elem.getExpr() instanceof Var && ((Var) elem.getExpr()).getName().equals(elem.getName())) {
                log.debug("ignoring self-aliasing of variable {}", elem.getName());
            } else {
                elements.add(elem);
            }
        }
    }

    @Override
    public void meet(Projection node) throws RuntimeException {
        // stop here, this is a subquery in SQL
    }

    @Override
    public void meet(Union node) throws RuntimeException {
        // stop here, this is a subquery in SQL
    }
}
