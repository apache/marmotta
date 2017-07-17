/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.sparql.function;

import org.eclipse.rdf4j.common.lang.service.ServiceRegistry;
import org.eclipse.rdf4j.model.IRI;

/**
 * Registry for natively supported functions
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class NativeFunctionRegistry extends ServiceRegistry<String,NativeFunction> {

    private static NativeFunctionRegistry defaultRegistry;

    /**
     * Gets the default FunctionRegistry.
     *
     * @return The default registry.
     */
    public static synchronized NativeFunctionRegistry getInstance() {
        if (defaultRegistry == null) {
            defaultRegistry = new NativeFunctionRegistry();
        }

        return defaultRegistry;
    }

    public NativeFunctionRegistry() {
        super(NativeFunction.class);
    }

    @Override
    protected String getKey(NativeFunction function)
    {
        return function.getURI();
    }

    public NativeFunction get(IRI uri) {
        return get(uri.stringValue()).get();
    }
}
