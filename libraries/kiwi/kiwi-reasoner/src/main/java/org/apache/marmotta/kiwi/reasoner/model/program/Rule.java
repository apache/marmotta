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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to represent a reasoning rule of the form
 *
 * BODY -> HEAD
 *
 * where BODY is a list of graph patterns with variables and
 * HEAD is a single graph pattern used for constructing new triples.
 * <p/>
 * Rules are always in disjunctive normal form, i.e. the BODY contains
 * only conjunctions of patterns and no disjunctions, and the HEAD
 * consists of only a single pattern.
 * <p/>
 * A graph pattern is of the form
 *
 * (S,P,O) or (S,P,O,C)
 *
 * where S is the subject, P the predicate, O the object, and C the context
 * of a triple. S, P, and O may be either URIs, abbreviated local names or
 * variables. C may only be a URI of abbreviated local name.
 * <p/>
 * Rules are "safe", i.e. variables occurring in the HEAD also have to
 * appear at least once in the body.
 *
 * User: sschaffe
 */
public class Rule {


    private long id = -1L;


    /**
     * The name of this rule. Mainly used for displaying it to the user.
     */
    private String name;

    /**
     * The human-readable description of this rule. Mainly used for displaying it to the user.
     */
    private String description;

    /**
     * The head of this rule as a pattern. Note that all variables occurring in the
     * head also have to occur at least once in the body of the rule.
     */
    private Pattern head;


    /**
     * The body of this rule as a list of patterns. Note that the order of patterns
     * is not guaranteed to be the same as specified when creating the rule. The reasoner
     * will execute patterns in the order it thinks is most appropriate.
     */
    private List<Pattern> body;



    public Rule() {
    }

    public Rule(long id) {
        this.id = id;
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

    public Pattern getHead() {
        return head;
    }

    public void setHead(Pattern head) {
        this.head = head;
    }

    public List<Pattern> getBody() {
        return body;
    }

    public void setBody(List<Pattern> body) {
        this.body = body;
    }


    public String toString() {
        StringBuilder s = new StringBuilder();

        for(Iterator<Pattern> it = getBody().iterator(); it.hasNext(); ) {
            s.append(it.next().toString());

            if(it.hasNext()) {
                s.append(", ");
            } else {
                s.append(" ");
            }
        }

        s.append("-> ");

        s.append(getHead().toString());

        return s.toString();
    }

    public String toString(Map<String,String> namespaces) {
        StringBuilder s = new StringBuilder();

        for(Iterator<Pattern> it = getBody().iterator(); it.hasNext(); ) {
            s.append(it.next().toString(namespaces));

            if(it.hasNext()) {
                s.append(", ");
            } else {
                s.append(" ");
            }
        }

        s.append("-> ");

        s.append(getHead().toString(namespaces));

        return s.toString();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;

        if(id >= 0) {
            return rule.id == id;
        } else {

            if (head != null ? !head.equals(rule.head) : rule.head != null) return false;
            if (name != null ? !name.equals(rule.name) : rule.name != null) return false;

            if(getBody() != null && rule.getBody() != null) {
                HashSet<Pattern> s1 = new HashSet<Pattern>(getBody());
                HashSet<Pattern> s2 = new HashSet<Pattern>(rule.getBody());

                return s1.equals(s2);
            } else {
                return getBody() == null && rule.getBody() == null;
            }
        }
    }

    @Override
    public int hashCode() {
        if(id >= 0) {
            return (int)id * 37;
        } else {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (head != null ? head.hashCode() : 0);
            result = 31 * result;
            if(getBody() != null) {
                for(Pattern pattern : getBody()) {
                    result += pattern.hashCode();
                }
            }
            return result;
        }
    }
}
