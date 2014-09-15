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

import org.openrdf.model.Resource;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

import java.util.ArrayList;
import java.util.List;

/**
 * A statement pattern translated to SQL consists of a named reference to the triple table, an indicator giving the
 * join type (JOIN or LEFT JOIN), and any number of filter conditions
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLPattern {


    /**
     * SQL conditions defined on this pattern; may only refer to previous or the current statement.
     */
    private List<String> conditions;

    /**
     * Maps triple patterns from SPARQL WHERE to SQL aliases for the TRIPLES table in the FROM part. Used
     * to join one instance of the triples table for each triple pattern occurring in the query.
     */
    private String name;

    /**
     * A reference to the SPARQL statement pattern represented by this SQLPattern
     */
    private StatementPattern sparqlPattern;

    /**
     * Alternative context values for each variable used in the context part of a pattern
     */
    private List<Resource> variableContexts;

    public SQLPattern(String name, StatementPattern sparqlPattern) {
        this.name = name;
        this.conditions = new ArrayList<>();
        this.conditions.add(name + ".deleted = false");
        this.sparqlPattern = sparqlPattern;
    }


    public Var[] getFields() {
        return new Var[] {
                getSparqlPattern().getSubjectVar(),
                getSparqlPattern().getPredicateVar(),
                getSparqlPattern().getObjectVar(),
                getSparqlPattern().getContextVar()
        };
    }


    public List<String> getConditions() {
        return conditions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StatementPattern getSparqlPattern() {
        return sparqlPattern;
    }

    public List<Resource> getVariableContexts() {
        return variableContexts;
    }

    public void setVariableContexts(List<Resource> variableContexts) {
        this.variableContexts = variableContexts;
    }
}
