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

import org.openrdf.query.algebra.Var;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a SPARQL variable in SQL.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SQLVariable {

    /**
     * A map for mapping the SPARQL variable names to internal names used for constructing SQL aliases.
     * Will look like { ?x -> "V1", ?y -> "V2", ... }
     */
    private String name;

    private Var sparqlVariable;

    /**
     * A map for mapping SPARQL variables to field names; each variable might have one or more field names,
     * depending on the number of patterns it occurs in; will look like
     * { ?x -> ["P1_V1", "P2_V1"], ?y -> ["P2_V2"], ... }
     */
    private List<String> aliases;


    /**
     * A map for mapping SPARQL variables to database node ID selectors. A node ID can occur either as
     * primary key in the NODES table or in the subject, predicate, object and context fields of a pattern.
     */
    private List<String> nodeIds;


    public SQLVariable(String name, Var sparqlVariable) {
        this.name = name;
        this.sparqlVariable = sparqlVariable;

        this.aliases = new ArrayList<>();
        this.nodeIds = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Var getSparqlVariable() {
        return sparqlVariable;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<String> getNodeIds() {
        return nodeIds;
    }
}
