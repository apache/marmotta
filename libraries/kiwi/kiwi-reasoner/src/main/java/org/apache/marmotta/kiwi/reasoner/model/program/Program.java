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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class Program {

    private long id = -1L;

    private String name;

    private String description;

    private Map<String,String> namespaces;

    private List<Rule> rules;


    public Program() {
        rules = new ArrayList<Rule>();
        namespaces = new HashMap<String, String>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public void addNamespace(String prefix, String uri) {
        getNamespaces().put(prefix,uri);
    }


    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }


    public void addRule(Rule rule) {
        getRules().add(rule);
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> ns : getNamespaces().entrySet()) {
            builder.append("@prefix ");
            builder.append(ns.getKey());
            builder.append(": <");
            builder.append(ns.getValue());
            builder.append(">\n");
        }
        if(getNamespaces().size() > 0) {
            builder.append("\n");
        }

        for(Rule rule : getRules()) {
            builder.append(rule.toString(getNamespaces()));
            builder.append("\n");

        }
        return builder.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Program program = (Program) o;

        if (description != null ? !description.equals(program.description) : program.description != null) return false;
        if (!name.equals(program.name)) return false;
        if (!namespaces.equals(program.namespaces)) return false;
        if (!rules.equals(program.rules)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + namespaces.hashCode();
        result = 31 * result + rules.hashCode();
        return result;
    }
}
