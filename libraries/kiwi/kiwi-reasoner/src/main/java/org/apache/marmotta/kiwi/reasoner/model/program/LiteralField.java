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

import org.apache.marmotta.kiwi.model.rdf.KiWiLiteral;
import org.openrdf.model.Literal;

import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class LiteralField implements Field {

    private Literal literal;

    public LiteralField() {
    }

    public LiteralField(Literal literal) {
        this.literal = literal;
    }

    public Literal getLiteral() {
        return literal;
    }

    public void setLiteral(KiWiLiteral literal) {
        this.literal = literal;
    }

    public String toString() {
        return "\"" + literal.getLabel() + "\"";
    }


    /**
     * Create string representation taking into account the namespace definitions given as argument.
     *
     * @param namespaces
     * @return
     */
    @Override
    public String toString(Map<String, String> namespaces) {
        return "\"" + literal.getLabel() + "\"";
    }

    @Override
    public boolean isResourceField() {
        return false;
    }

    @Override
    public boolean isLiteralField() {
        return true;
    }

    @Override
    public boolean isVariableField() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LiteralField that = (LiteralField) o;

        if (literal != null ? !literal.equals(that.literal) : that.literal != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return literal != null ? literal.hashCode() : 0;
    }
}
