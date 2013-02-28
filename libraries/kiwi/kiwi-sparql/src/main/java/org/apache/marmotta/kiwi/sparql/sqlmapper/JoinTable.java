/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.sparql.services.evaluation.sql;

import org.jooq.Condition;
import org.jooq.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper for representing information about a table to join with. Holds a reference to the table and a
 * list of conditions to be used as join conditions.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class JoinTable {

    private Table table;

    private Set<Condition> conditions;

    // remember conditions referring to tables not created in the current statement; they will also be contained
    // in conditions, this set is just for checking in special cases
    private Set<Condition> backConditions;


    public JoinTable(Table table) {
        this.table = table;

        conditions     = new HashSet<Condition>();
        backConditions = new HashSet<Condition>();
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public Set<Condition> getConditions() {
        return conditions;
    }

    public void addCondition(Condition condition) {
        conditions.add(condition);
    }

    public Set<Condition> getBackConditions() {
        return backConditions;
    }

    public void addBackCondition(Condition condition) {
        backConditions.add(condition);
    }
}
