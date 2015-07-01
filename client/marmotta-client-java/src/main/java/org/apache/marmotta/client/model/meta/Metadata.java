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
package org.apache.marmotta.client.model.meta;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.marmotta.client.model.rdf.Literal;
import org.apache.marmotta.client.model.rdf.RDFNode;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class Metadata extends HashMap<String,Set<RDFNode>> {
    
	private static final long serialVersionUID = 1L;

	private String subject;

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public Metadata(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }
    
    public RDFNode getFirst(String propertyUri) {
        Preconditions.checkNotNull(get(propertyUri));
        Preconditions.checkState(get(propertyUri).iterator().hasNext());

        return get(propertyUri).iterator().next();
    }


    /**
     * Convert a more simple property map into a metadata representation. Note that all keys of the property map still
     * are required to be valid RDF URI resources. All properties will have string literal values in the resulting metadata.
     *
     * @param resource
     * @param map
     * @return
     */
    public static Metadata fromPropertiesMap(String resource, Map<String,String> map) {
        Metadata m = new Metadata(resource);
        for(Map.Entry<String,String> entry : map.entrySet()) {
            m.put(entry.getKey(), ImmutableSet.<RDFNode>of(new Literal(entry.getValue())));
        }
        return m;
    }

    /**
     * Convert a metadata representation into a simpler property map, potentially loosing information. Only the
     * first literal value of a property is copied to the resulting map.
     *
     * @param metadata
     * @return
     */
    public static Map<String,String> toPropertiesMap(Metadata metadata) {
        Map<String,String> result = new HashMap<String, String>();
        for(Map.Entry<String,Set<RDFNode>> entry : metadata.entrySet()) {
            for(RDFNode n : entry.getValue())  {
                if(n instanceof Literal) {
                    result.put(entry.getKey(),((Literal) n).getContent());
                    break;
                }
            }
        }
        return result;
    }
}
