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

import java.util.ArrayList;
import java.util.List;

/**
 * An SQL fragment is a part of the SQL query where all patterns are joinedwith  INNER JOINS and not LEFT JOINS. Several
 * patterns are then joined using a left join.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLFragment {

    /**
     * The patterns contained in this fragment. All patterns are joined using an INNER JOIN.
     */
    private List<SQLPattern> patterns;

    private List<String> conditions;

    private List<ValueExpr> filters;


    public SQLFragment() {
        this.patterns   = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.filters    = new ArrayList<>();
    }

    public List<SQLPattern> getPatterns() {
        return patterns;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public List<ValueExpr> getFilters() {
        return filters;
    }
}