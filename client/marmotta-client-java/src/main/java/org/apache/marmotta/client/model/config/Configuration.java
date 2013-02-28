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
package org.apache.marmotta.client.model.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class Configuration {
    
    private String key;
    
    private Object value;


    public Configuration(String key, Object value) {
        this.key = key;
        this.value = value;
    }


    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
    
    public String getString() {
        if(value instanceof Collection) {
            if(((Collection<?>) value).isEmpty()) {
                return null;   
            } else {
                return ((Collection<?>) value).iterator().next().toString();
            }
        } else {
            return value.toString();
        }
    }
    
    public List<String> getList() {
        List<String> result = new ArrayList<String>();
        if(value instanceof Collection) {
            for(Object o : (Collection<?>) value) {
                result.add(o.toString());
            }
        } else {
            result.add(value.toString());
        }
        return result;
    }
}
