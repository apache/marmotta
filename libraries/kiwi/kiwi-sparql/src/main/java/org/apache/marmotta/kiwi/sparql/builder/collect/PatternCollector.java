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
import org.apache.marmotta.kiwi.sparql.builder.ValueConverter;
import org.apache.marmotta.kiwi.sparql.builder.model.SQLFragment;
import org.apache.marmotta.kiwi.sparql.builder.model.SQLPattern;
import org.apache.marmotta.kiwi.sparql.builder.model.SQLSubQuery;
import org.apache.marmotta.kiwi.sparql.builder.model.SQLUnion;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import java.util.LinkedList;
import java.util.Set;

/**
* Collect all statement patterns in a tuple expression.
*
* @author Sebastian Schaffert (sschaffert@apache.org)
*/
public class PatternCollector extends QueryModelVisitorBase<RuntimeException> {

    public LinkedList<SQLFragment> parts   = new LinkedList<>();

    int counter = 0;


    private BindingSet bindings;
    private Dataset dataset;
    private ValueConverter converter;
    private KiWiDialect dialect;
    private Set<String> projectedVars;
    private String prefix;

    public PatternCollector(TupleExpr expr, BindingSet bindings, Dataset dataset, ValueConverter converter, KiWiDialect dialect, Set<String> projectedVars, String prefix) {
        this.bindings = bindings;
        this.dataset = dataset;
        this.converter = converter;
        this.dialect = dialect;
        this.projectedVars = projectedVars;
        this.prefix  = prefix;

        parts.push(new SQLFragment());
        expr.visit(this);
    }

    @Override
    public void meet(StatementPattern node) throws RuntimeException {
        parts.getLast().getPatterns().add(new SQLPattern(prefix + "P" + (++counter), node));

        super.meet(node);
    }

    @Override
    public void meet(LeftJoin node) throws RuntimeException {
        node.getLeftArg().visit(this);
        parts.addLast(new SQLFragment());
        if(node.hasCondition()) {
            parts.getLast().getFilters().add(node.getCondition());
        }
        node.getRightArg().visit(this);

    }


    @Override
    public void meet(Filter node) throws RuntimeException {
        parts.getLast().getFilters().add(node.getCondition());

        if(node.getArg() instanceof Group) {
            parts.getLast().setConditionPosition(SQLFragment.ConditionPosition.HAVING);
        }

        super.meet(node);
    }

    @Override
    public void meet(Union node) throws RuntimeException {
        // unions are treated as subqueries, don't continue collection, but add the Union to the last part

        parts.getLast().getSubqueries().add(new SQLUnion(prefix + "U" + (++counter),node, bindings, dataset, converter, dialect));
    }

    @Override
    public void meet(Projection node) throws RuntimeException {
        // subqueries are represented with a projection inside a JOIN; we don't continue collection

        parts.getLast().getSubqueries().add(new SQLSubQuery(prefix + "S" + (++counter), node, bindings, dataset, converter, dialect, projectedVars));
    }

    @Override
    public void meet(Exists node) throws RuntimeException {
        // stop at exists, it is treated as a subquery in the condition part
    }
}
