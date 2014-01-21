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

package org.apache.marmotta.commons.sesame.model;

import com.google.common.base.Function;
import org.openrdf.model.Value;

/**
 * Utility functions for working with values.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ValueCommons {

    public static Function<Value,String> stringValue() {
        return STRING_VALUE;
    }



    private static Function<Value, String> STRING_VALUE = new Function<Value, String>() {
        @Override
        public String apply(Value input) {
            return input.stringValue();
        }
    };

}
