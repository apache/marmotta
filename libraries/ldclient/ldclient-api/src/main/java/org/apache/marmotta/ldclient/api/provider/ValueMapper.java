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
package org.apache.marmotta.ldclient.api.provider;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.util.List;

/**
 * Map the value returned by a selection in a different data format (e.g. an XPath expression( into a list of Sesame
 * values, using the ValueFactory passed as argument.
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface ValueMapper {

    /**
     * Take the selected value, process it according to the mapping definition, and create Sesame Values using the
     * factory passed as argument.
     *
     *
     * @param resourceUri
     * @param selectedValue
     * @param factory
     * @return
     */
    public List<Value> map(String resourceUri, String selectedValue, ValueFactory factory);
}
