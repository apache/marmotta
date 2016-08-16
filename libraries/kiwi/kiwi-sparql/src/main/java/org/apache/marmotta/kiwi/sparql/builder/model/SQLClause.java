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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Used to mark fragments that can be used to construct table-like FROM clauses in SQL queries (triple patterns,
 * subqueries, unions, ...)
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class SQLClause {

    /**
     * SQL conditions defined on this pattern; may only refer to previous or the current statement.
     */
    protected List<String> conditions;

    public SQLClause() {
        this.conditions = new ArrayList<>();
    }

    /**
     * Build the query fragment that can be used in the FROM clause of a SQL query for representing this SPARQL construct.
     * The fragment will be joined appropriately by the enclosing construct using CROSS JOIN, LEFT JOIN or normal JOIN.
     *
     * @return
     */
    public abstract String buildFromClause();

    /**
     * Return true if the FROM clause requires parenthesis before
     * @return
     */
    public boolean needsParentheses() {
        return false;
    }


    /**
     * Add a SQL condition to the list of conditions.
     * @param condition
     */
    public void addCondition(String condition) {
        conditions.add(condition);
    }

    /**
     * Build the condition clause for this statement to be used in the WHERE part or the ON part of a JOIN.
     * @return
     */
    public String buildConditionClause() {
        // the onClause consists of the filter conditions from the statement for joining/left joining with
        // previous statements
        StringBuilder onClause = new StringBuilder();

        for(Iterator<String> cit = conditions.iterator(); cit.hasNext(); ) {
            String next = cit.next();
            if(onClause.length() > 0 && next.length() > 0) {
                onClause.append("\n      AND ");
            }
            onClause.append(next);
        }

        return onClause.toString();
    }


    public List<String> getConditions() {
        return conditions;
    }
}
