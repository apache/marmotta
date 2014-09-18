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

/**
 * Mapping for a variable between its name in a subquery and its name in the parent query. Used for resolving join
 * fields in subqueries.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class VariableMapping {

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
