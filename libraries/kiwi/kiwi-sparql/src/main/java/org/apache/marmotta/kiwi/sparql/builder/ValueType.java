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

package org.apache.marmotta.kiwi.sparql.builder;

/**
* Operand types for operations - used for implicit type coercion.
*
* @author Sebastian Schaffert (sschaffert@apache.org)
*/
public enum ValueType {
    DOUBLE, // double or float value
    DECIMAL,// decimal value
    INT,    // long or integer value
    DATE,   // UTC date, dateTime or time
    TZDATE, // date, dateTime or time with timezone
    BOOL,   // boolean value
    STRING, // string value
    NODE,   // database node ID of existing node
    TERM,   // value of constructed term
    URI,    // constructed URI
    BNODE,  // constructed BNODE
    NONE;    // not projected


    public final boolean isNumeric() {
        return this == DOUBLE || this == DECIMAL || this == INT;
    }
}
