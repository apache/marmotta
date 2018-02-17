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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.query.algebra.ExtensionElem;
import org.eclipse.rdf4j.query.algebra.Group;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Union;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Find the offset and limit values in a tuple expression
*
* @author Sebastian Schaffert (sschaffert@apache.org)
*/
public class SQLProjectionFinder extends AbstractQueryModelVisitor<RuntimeException> {

    private static Logger log = LoggerFactory.getLogger(SQLProjectionFinder.class);

    public List<ExtensionElem> elements = new ArrayList<>();

    private String needle;

    public boolean found = false;

    public SQLProjectionFinder(TupleExpr expr, String needle) {
        this.needle = needle;
        expr.visit(this);
    }

    @Override
    public void meet(ExtensionElem node) throws RuntimeException {
        if(node.getName().equals(needle)) {
            found = true;
        }
        // don't recurse to the children, as this would project non-grouped elements
    }

    @Override
    public void meet(Group node) throws RuntimeException {
        for(String g : node.getGroupBindingNames()) {
            if(g.equals(needle)) {
                found = true;
            }
        }
        // don't recurse to the children, as this would project non-grouped elements
    }

    @Override
    public void meet(Var node) throws RuntimeException {
        if(node.getName().equals(needle)) {
            found = true;
        }
    }


    @Override
    public void meet(Projection node) throws RuntimeException {
        for(ProjectionElem elem : node.getProjectionElemList().getElements()) {
            if(elem.getSourceName().equals(needle)) {
                found = true;
            }
        }
        // stop at projection, subquery
    }

    @Override
    public void meet(Union node) throws RuntimeException {
        // stop at union, subquery
    }
}
