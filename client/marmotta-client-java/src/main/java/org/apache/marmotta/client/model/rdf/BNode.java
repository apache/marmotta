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
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class BNode extends RDFNode { 
    
    private String anonId;

    public BNode(String anonId) {
        this.anonId = anonId;
    }

    public String getAnonId() {
        return anonId;
    }

    public void setAnonId(String anonId) {
        this.anonId = anonId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BNode bNode = (BNode) o;

        if (anonId != null ? !anonId.equals(bNode.anonId) : bNode.anonId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return anonId != null ? anonId.hashCode() : 0;
    }
}
