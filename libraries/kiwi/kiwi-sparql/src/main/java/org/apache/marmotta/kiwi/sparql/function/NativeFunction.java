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

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.sparql.builder.OPTypes;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * Specification of extended native function support for KiWi. Subclasses represent Sesame SPARQL functions
 * that can be translated into native SQL according to the different dialects.
 *
 * This interface extends Function because we want to ensure that every function really also corresponds
 * to a Sesame function.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface NativeFunction extends Function {

    /**
     * Return true if this function has available native support for the given dialect
     * @param dialect
     * @return
     */
    public boolean isSupported(KiWiDialect dialect);

    /**
     * Return a string representing how this function is translated into SQL in the given dialect
     * @param dialect
     * @param args
     * @return
     */
    public String getNative(KiWiDialect dialect, String... args);

    /**
     * Get the return type of the function. This is needed for SQL type casting inside KiWi.
     * @return
     */
    public OPTypes getReturnType();

    /**
     * Get the argument type of the function for the arg'th argument (starting to count at 0).
     * This is needed for SQL type casting inside KiWi.
     *
     * @param arg
     * @return
     */
    public OPTypes getArgumentType(int arg);

    /**
     * Return the minimum number of arguments this function requires.
     * @return
     */
    public int getMinArgs();

    /**
     * Return the maximum number of arguments this function can take
     *
     * @return
     */
    public int getMaxArgs();
}
