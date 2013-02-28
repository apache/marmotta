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
package org.apache.marmotta.client.model.rdf;

/**
 * A lightweight RDF Literal implementation providing the base functionalities.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class Literal extends RDFNode {
    
    private String content;
    
    private String language;
    
    private URI type;


    public Literal(String content) {
        this.content = content;
    }

    public Literal(String content, String language) {
        this.content = content;
        this.language = language;
    }

    public Literal(String content, URI type) {
        this.content = content;
        this.type = type;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }
    
    
    public int getInt() {
        return Integer.parseInt(content);
    }
    
    public long getLong() {
        return Long.parseLong(content);
    }

    public double getDouble() {
        return Double.parseDouble(content);
    }

    public float getFloat() {
        return Float.parseFloat(content);
    }

    public boolean getBoolean() {
        return Boolean.getBoolean(content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Literal literal = (Literal) o;

        if (!content.equals(literal.content)) return false;
        if (language != null ? !language.equals(literal.language) : literal.language != null) return false;
        if (type != null ? !type.equals(literal.type) : literal.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return content;
    }
}

