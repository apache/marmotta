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

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class ResourceField implements Field {

    private Resource resource;

    public ResourceField(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String toString() {
        return toString(null);
    }

    /**
     * Create string representation taking into account the namespace definitions given as argument.
     *
     * @param namespaces
     * @return
     */
    @Override
    public String toString(Map<String, String> namespaces) {
        if(getResource() instanceof URI) {
            String uri = getResource().stringValue();
            if(namespaces != null) {
                for(Map.Entry<String,String> ns : namespaces.entrySet()) {
                    if(uri.startsWith(ns.getValue())) {
                        return ns.getKey() + ":" + uri.substring(ns.getValue().length());
                    }
                }
            }
            return "<" + uri + ">";
        } else if(getResource() instanceof BNode) {
            return "_:"+getResource().stringValue();
        } else {
            return null;
        }
    }


    @Override
    public boolean isResourceField() {
        return true;
    }

    @Override
    public boolean isLiteralField() {
        return false;
    }

    @Override
    public boolean isVariableField() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceField that = (ResourceField) o;

        if (resource != null ? !resource.equals(that.resource) : that.resource != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return resource != null ? resource.hashCode() : 0;
    }

}
