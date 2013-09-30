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
 * Representation of a graph pattern. A graph pattern has a subject, predicate, and object, and
 * optionally a context. Subject, predicate and object may be either RDF nodes or variables or null.
 * Context is either null or a RDF resource.
 *
 *
 * User: sschaffe
 */
public class Pattern  {


    private Field subject;

    private Field property;

    private Field object;

    private Field context;

    public Pattern() {
    }


    public Pattern(Field subject, Field property, Field object) {
        this.setSubject(subject);
        this.setProperty(property);
        this.setObject(object);
    }


    public Field getSubject() {
        return subject;
    }

    public void setSubject(Field subject) {
        this.subject = subject;
    }

    public Field getProperty() {
        return property;
    }

    public void setProperty(Field property) {
        this.property = property;
    }

    public Field getObject() {
        return object;
    }

    public void setObject(Field object) {
        this.object = object;
    }

    public Field getContext() {
        return context;
    }

    public void setContext(Field context) {
        this.context = context;
    }

    public String toString() {
        return "(" + getSubject().toString() + " " + getProperty().toString() + " " + getObject().toString() + ")";
    }

    public String toString(Map<String,String> namespaces) {
        return "(" + getSubject().toString(namespaces) + " " + getProperty().toString(namespaces) + " " + getObject().toString(namespaces) + ")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if(context == null && object == null && subject == null && property == null) {
            return super.equals(o);
        }

        Pattern pattern = (Pattern) o;

        if (context != null ? !context.equals(pattern.context) : pattern.context != null) return false;
        if (object != null ? !object.equals(pattern.object) : pattern.object != null) return false;
        if (property != null ? !property.equals(pattern.property) : pattern.property != null) return false;
        if (subject != null ? !subject.equals(pattern.subject) : pattern.subject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subject != null ? subject.hashCode() : 0;
        result = 31 * result + (property != null ? property.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }
}
