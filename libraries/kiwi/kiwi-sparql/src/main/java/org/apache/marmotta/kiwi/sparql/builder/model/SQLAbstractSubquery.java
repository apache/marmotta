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

import java.util.HashSet;
import java.util.Set;

/**
 * Common fields and methods for all subqueries.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class SQLAbstractSubquery extends SQLClause {

    protected String alias;


    /**
     * This set contains variable names describing those variables of the subquery that need to be joined with the NODES
     * table. This is typically the case when there is a condition or function referring to the actual value of the node
     * and not only the ID. The joined NODES table will be aliased with {alias}_{name}.
     */
    private Set<VariableMapping> joinFields = new HashSet<>();


    public SQLAbstractSubquery(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }


    public Set<VariableMapping> getJoinFields() {
        return joinFields;
    }

    /**
     * Return true when the pattern involves JOINs with the NODES table; in this case we need to enclose the
     * FROM clause with parentheses before joining with previous clauses.
     * @return
     */
    public boolean hasJoinFields() {
        return joinFields.size() > 0;
    }


    /**
     * Return true if the FROM clause requires parenthesis before
     *
     * @return
     */
    @Override
    public boolean needsParentheses() {
        return hasJoinFields();
    }


    /**
     * Return the SQL variables used by the subquery; we need this to do proper mapping in the parent query.
     * @return
     */
    public abstract Set<SQLVariable> getQueryVariables();

    /**
     * Mapping for a variable between its name in a subquery and its name in the parent query. Used for resolving join
     * fields in subqueries.
     *
     * @author Sebastian Schaffert (sschaffert@apache.org)
     */
    public static class VariableMapping {

        private String parentName, subqueryName;

        public VariableMapping(String parentName, String subqueryName) {
            this.parentName = parentName;
            this.subqueryName = subqueryName;
        }

        public String getParentName() {
            return parentName;
        }

        public String getSubqueryName() {
            return subqueryName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VariableMapping that = (VariableMapping) o;

            if (!parentName.equals(that.parentName)) return false;
            if (!subqueryName.equals(that.subqueryName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = parentName.hashCode();
            result = 31 * result + subqueryName.hashCode();
            return result;
        }
    }
}
