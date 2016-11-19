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

import org.openrdf.model.Resource;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A statement pattern translated to SQL consists of a named reference to the triple table, an indicator giving the
 * join type (JOIN or LEFT JOIN), and any number of filter conditions
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLPattern extends SQLClause {


    /**
     * Describe the different columns of the triple table that we might need to join with
     */
    public enum TripleColumns {
        SUBJECT  ("subject"),
        PREDICATE("predicate"),
        OBJECT   ("object"),
        CONTEXT  ("context");

        TripleColumns(String fieldName) {
            this.fieldName = fieldName;
        }

        private final String fieldName;

        public String getFieldName() {
            return fieldName;
        }
    }

    /**
     * This map contains mappings from column to variable names. If for a given column an entry is contained in the
     * map, the statement requires to join the TRIPLE table on this field with the NODES table. This is typically
     * the case when there is a condition or function referring to the actual value of the node and not only the ID.
     * The joined NODES table will be aliased with the variable name contained as value for the field.
     */
    private EnumMap<TripleColumns, String> joinFields = new EnumMap<>(TripleColumns.class);

    /**
     * A map containing references to the variables used in the triple fields.
     */
    private EnumMap<TripleColumns, Var> tripleFields = new EnumMap<>(TripleColumns.class);

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
        super();
        this.name = name;
        this.conditions.add(name + ".deleted = false");
        this.sparqlPattern = sparqlPattern;

        tripleFields.put(TripleColumns.SUBJECT,   sparqlPattern.getSubjectVar());
        tripleFields.put(TripleColumns.PREDICATE, sparqlPattern.getPredicateVar());
        tripleFields.put(TripleColumns.OBJECT,    sparqlPattern.getObjectVar());
        tripleFields.put(TripleColumns.CONTEXT,   sparqlPattern.getContextVar());
    }

    /**
     * Set the variable name (alias for the NODES table) for the given column to "varName".
     * @param col
     * @param varName
     */
    public void setJoinField(TripleColumns col, String varName) {
        joinFields.put(col,varName);
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

    public Var[] getFields() {
        return new Var[] {
                getSparqlPattern().getSubjectVar(),
                getSparqlPattern().getPredicateVar(),
                getSparqlPattern().getObjectVar(),
                getSparqlPattern().getContextVar()
        };
    }

    public EnumMap<TripleColumns, Var> getTripleFields() {
        return tripleFields;
    }

    /**
     * Create the clause to be used in the FROM part to represent this pattern and return it. The name and join fields
     * need to be set before so this produces the correct output.
     *
     * This method works as follows:
     * - for the statement pattern, it adds a reference to the TRIPLES table and aliases it with the pattern name
     * - for each field of the pattern whose value is used in conditions or functions, an INNER JOIN with the NODES table
     *   is added to retrieve the actual node with its values; the NODES table is aliased using the variable name set in
     *   setJoinField()
     *
     * @return
     */
    public String buildFromClause() {
        // the joinClause consists of a reference to the triples table and possibly inner joins with the
        // nodes table in case we need to verify node values, e.g. in a filter
        StringBuilder fromClause = new StringBuilder();


        fromClause.append("triples " + name);


        for(Map.Entry<TripleColumns,String> colEntry : joinFields.entrySet()) {
            TripleColumns col = colEntry.getKey();
            String        var = colEntry.getValue();

            fromClause.append("\n    INNER JOIN nodes AS ");
            fromClause.append(name + "_" + col.getFieldName() + "_" + var);

            fromClause.append(" ON " + name + "." + col.getFieldName() + " = " + name + "_" + col.getFieldName() + "_" + var + ".id ");

        }


        return fromClause.toString();
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
