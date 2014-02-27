/*
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
package org.apache.marmotta.platform.ldp.patch.model;

import org.apache.commons.lang3.ObjectUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * The Statement in a RdfPatch.
 * {@code null}-Values represent {@code R}epeat from the <a href="http://afs.github.io/rdf-patch/#rdf-patch-details">Spec</a>.
 */
public class WildcardStatement implements Statement {


    private final Value object;
    private final URI predicate;
    private final Resource subject;

    public WildcardStatement(Resource subject, URI predicate, Value object) {
        this.object = object;
        this.predicate = predicate;
        this.subject = subject;
    }

    @Override
    public Resource getSubject() {
        return subject;
    }

    @Override
    public URI getPredicate() {
        return predicate;
    }

    @Override
    public Value getObject() {
        return object;
    }

    @Override
    public Resource getContext() {
        return null;
    }

    @Override
    public int hashCode() {
        return 961 * (subject!=null?subject.hashCode():0) + 31 * (predicate!=null?predicate.hashCode():0) + (object!=null?object.hashCode():0);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Statement) {
            Statement otherSt = (Statement)other;

            // The object is potentially the cheapest to check, as types
            // of these references might be different.

            // In general the number of different predicates in sets of
            // statements is the smallest, so predicate equality is checked
            // last.
            return ObjectUtils.equals(object, otherSt.getObject()) && ObjectUtils.equals(subject, otherSt.getSubject())
                    && ObjectUtils.equals(predicate, otherSt.getPredicate());
        }

        return false;
    }

    /**
     * Gives a String-representation of this Statement that can be used for
     * debugging.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);

        sb.append("(");
        sb.append(getSubject());
        sb.append(", ");
        sb.append(getPredicate());
        sb.append(", ");
        sb.append(getObject());
        sb.append(")");

        return sb.toString();
    }
}
