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

import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class VariableField implements Field {

    String name;

    public VariableField() {
    }

    public VariableField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getName();
    }

    /**
     * Create string representation taking into account the namespace definitions given as argument.
     *
     * @param namespaces
     * @return
     */
    @Override
    public String toString(Map<String, String> namespaces) {
        return getName();
    }

    @Override
    public boolean isResourceField() {
        return false;
    }

    @Override
    public boolean isLiteralField() {
        return false;
    }

    @Override
    public boolean isVariableField() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariableField that = (VariableField) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
