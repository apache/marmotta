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

package org.apache.marmotta.kiwi.sparql.builder.model;

import org.apache.marmotta.kiwi.sparql.builder.ValueType;
import org.openrdf.query.algebra.ValueExpr;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Representation of a SPARQL variable in SQL. A SPARQL variable will always be translated into a column alias
 * for either a subject/predicate/object/context field of a triple or a more complex expression (e.g. function evaluation).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLVariable  implements Cloneable{

    /**
     * A map for mapping the SPARQL variable names to internal names used for constructing SQL aliases.
     * Will look like { ?x -> "V1", ?y -> "V2", ... }
     */
    private String name;

    private String sparqlName;

    /**
     * A map for mapping SPARQL variables to field names; each variable might have one or more field names,
     * depending on the number of patterns it occurs in; will look like
     * { ?x -> ["P1_V1", "P2_V1"], ?y -> ["P2_V2"], ... }
     */
    private String alias;


    /**
     * A map for mapping SPARQL variables to database expressions (e.g. node ID selectors). A node ID can occur either as
     * primary key in the NODES table or in the subject, predicate, object and context fields of a pattern. An expression
     * can be e.g. a function evaluation.
     */
    private List<String> expressions;

    /**
     * A list of value expressions bound to this variable; this is needed in case the variable is used in a filter or
     * ORDER BY, because then we need to determine the type.
     */
    private List<ValueExpr> bindings;


    /**
     * Set to something else than NONE when this variable is contained in the SELECT part of the query, i.e. needs to be projected.
     * Decides on how the variable will be projected (as node -> ID, as value -> string or numeric field)
     */
    private ValueType projectionType = ValueType.NONE;

    /**
     * The expression to project the type for the literal that will be bound to this variable, e.g. in case
     * the main expression is a function call and the type should be preserved.
     */
    private String literalTypeExpression = null;

    /**
     * The expression to project the language for the literal that will be bound to this variable, e.g. in case
     * the main expression is a function call and the language should be preserved.
     */
    private String literalLangExpression = null;

    public SQLVariable(String name, String sparqlName) {
        this.name = name;
        this.sparqlName = sparqlName;

        this.bindings = new ArrayList<>();
        this.expressions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSparqlName() {
        return sparqlName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<ValueExpr> getBindings() {
        return bindings;
    }

    public List<String> getExpressions() {
        return expressions;
    }

    public ValueType getProjectionType() {
        return projectionType;
    }

    public void setProjectionType(ValueType projectionType) {
        this.projectionType = projectionType;
    }

    public String getLiteralTypeExpression() {
        return literalTypeExpression;
    }

    public void setLiteralTypeExpression(String literalTypeExpression) {
        this.literalTypeExpression = literalTypeExpression;
    }

    public String getLiteralLangExpression() {
        return literalLangExpression;
    }

    public void setLiteralLangExpression(String literalLangExpression) {
        this.literalLangExpression = literalLangExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLVariable that = (SQLVariable) o;

        if (!sparqlName.equals(that.sparqlName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sparqlName.hashCode();
    }

    @Override
    public String toString() {
        return "Variable{" +
                "SQL name='" + name + '\'' +
                ", SPARQL name=" + sparqlName +
                ", alias=" + alias +
                ", expressions=" + expressions +
                ", projectionType=" + projectionType +
                '}';
    }


    public static final Comparator<SQLVariable> sparqlNameComparator = new Comparator<SQLVariable>() {
        @Override
        public int compare(SQLVariable l, SQLVariable r) {
            return Collator.getInstance().compare(l.getSparqlName(), r.getSparqlName());
        }
    };

    public static final Comparator<SQLVariable> sqlNameComparator = new Comparator<SQLVariable>() {
        @Override
        public int compare(SQLVariable l, SQLVariable r) {
            return Collator.getInstance().compare(l.getName(), r.getName());
        }
    };

    @Override
    public Object clone() throws CloneNotSupportedException {
        SQLVariable clone = new SQLVariable(getName(), getSparqlName());
        clone.projectionType = projectionType;
        clone.getExpressions().addAll(expressions);
        clone.alias = alias;
        clone.getBindings().addAll(bindings);

        return clone;
    }
}
