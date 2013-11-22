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
package org.apache.marmotta.kiwi.reasoner.model.program;


import com.google.common.base.Equivalence;
import org.apache.marmotta.commons.sesame.model.StatementCommons;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.openrdf.model.Statement;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Reason Maintenance: A justification for the existence of a triple. The justification
 * contains a reference to the triple it supports and references to the triples and
 * rules that support this justification.
 * <p/>
 * A justification is created and stored each time a reasoning rule infers new triples.
 * Each of the inferred triples will be supported by the rules and base triples that
 * were used in the inference. This makes both explanations and maintenance much simpler,
 * i.e. when a base triple is deleted, we can remove all justifications that contain it
 * and then remove all inferred triples that are no longer supported by any justification.
 *
 * <p/>
 * User: sschaffe
 */
public class Justification  {

    private long id = -1L;

    /**
     * The triple that is supported by this justification
     */
    private KiWiTriple triple;

    /**
     * The base triples that support this justification
     */
    private Set<KiWiTriple> supportingTriples;


    /**
     * The rules that support this justification
     */
    private Set<Rule> supportingRules;

    /**
     * The date when the justification was created (stored in the database)
     */
    private Date createdAt;


    private static Equivalence<Statement> equivalence = StatementCommons.quadrupleEquivalence();

    public Justification() {
        supportingTriples = StatementCommons.newQuadrupleSet();
        supportingRules   = new HashSet<Rule>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public KiWiTriple getTriple() {
        return triple;
    }

    public void setTriple(KiWiTriple triple) {
        this.triple = triple;
    }

    public Set<KiWiTriple> getSupportingTriples() {
        return supportingTriples;
    }

    public void setSupportingTriples(Set<KiWiTriple> supportingTriples) {
        this.supportingTriples = supportingTriples;
    }

    public Set<Rule> getSupportingRules() {
        return supportingRules;
    }

    public void setSupportingRules(Set<Rule> supportingRules) {
        this.supportingRules = supportingRules;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Justification that = (Justification) o;

        //if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!supportingRules.equals(that.supportingRules)) return false;
        if (!supportingTriples.equals(that.supportingTriples)) return false;
        if (!equivalence.equivalent(this.triple, that.triple)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0; // id != null ? id.hashCode() : 0;
        result = 31 * result + equivalence.hash(triple);
        result = 31 * result + supportingTriples.hashCode();
        result = 31 * result + supportingRules.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Justification{" +
                "triple=" + triple +
                ", supportingTriples=" + supportingTriples +
                ", supportingRules=" + supportingRules +
                '}';
    }
}
