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

package org.apache.marmotta.kiwi.sparql.builder;

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
    private List<String> aliases;


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
    private ProjectionType projectionType = ProjectionType.NONE;

    public SQLVariable(String name, String sparqlName) {
        this.name = name;
        this.sparqlName = sparqlName;

        this.aliases = new ArrayList<>();
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

    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Primary alias for a variable, used e.g. when projecting or evaluating in functions. All others are added with join conditions.
     * @return
     */
    public String getPrimaryAlias() {
        return aliases.get(0);
    }

    public List<ValueExpr> getBindings() {
        return bindings;
    }

    public List<String> getExpressions() {
        return expressions;
    }

    public ProjectionType getProjectionType() {
        return projectionType;
    }

    public void setProjectionType(ProjectionType projectionType) {
        this.projectionType = projectionType;
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
                ", aliases=" + aliases +
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
    protected Object clone() throws CloneNotSupportedException {
        SQLVariable clone = new SQLVariable(getName(), getSparqlName());
        clone.projectionType = projectionType;
        clone.getExpressions().addAll(expressions);
        clone.getAliases().addAll(aliases);
        clone.getBindings().addAll(bindings);

        return clone;
    }
}
