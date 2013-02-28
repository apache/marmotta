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
package org.apache.marmotta.client.model.sparql;


import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.marmotta.client.model.rdf.RDFNode;

/**
 * Provides a list of result bindings and information about the available fields.
 * 
 * @author Sebastian Schaffert
 */
public class SPARQLResult extends LinkedList<Map<String,RDFNode>> {
    
    private static final long serialVersionUID = -527039638847863378L;
    
    private Set<String> fieldNames;

    /**
     * Constructs an empty list.
     */
    public SPARQLResult(Set<String> fieldNames) {
        this.fieldNames = fieldNames;
    }


    public Set<String> getFieldNames() {
        return fieldNames;
    }
    
}
