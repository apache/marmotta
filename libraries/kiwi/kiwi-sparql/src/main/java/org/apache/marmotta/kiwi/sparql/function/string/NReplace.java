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

package org.apache.marmotta.kiwi.sparql.function.string;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sparql.builder.ValueType;
import org.apache.marmotta.kiwi.sparql.function.NativeFunction;
import org.openrdf.query.algebra.evaluation.function.string.Replace;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class NReplace extends Replace implements NativeFunction {

    /**
     * Return true if this function has available native support for the given dialect
     *
     * @param dialect
     * @return
     */
    @Override
    public boolean isSupported(KiWiDialect dialect) {
        return dialect instanceof PostgreSQLDialect || dialect instanceof H2Dialect;
    }

    /**
     * Return a string representing how this function is translated into SQL in the given dialect
     *
     * @param dialect
     * @param args
     * @return
     */
    @Override
    public String getNative(KiWiDialect dialect, String... args) {
        if (dialect instanceof PostgreSQLDialect) {
            if(args.length == 3) {
                // no flags
                return String.format("regexp_replace(%s, %s, %s, 'g')", args[0], args[1], args[2]);
            } else if(args.length == 4) {
                // flags
                StringBuilder psqlFlags = new StringBuilder();
                if(StringUtils.containsIgnoreCase(args[3], "i")) {
                    psqlFlags.append("i");
                }

                return String.format("regexp_replace(%s, %s, %s, 'g%s')", args[0], args[1], args[2], psqlFlags.toString());
            }
        } else if (dialect instanceof H2Dialect) {
            // no flags
            return String.format("REGEXP_REPLACE(%s, %s, %s)", args[0], args[1], args[2]);
        }
        throw new UnsupportedOperationException("REPLACE not supported in dialect "+dialect);
    }

    /**
     * Get the return type of the function. This is needed for SQL type casting inside KiWi.
     *
     * @return
     */
    @Override
    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    /**
     * Get the argument type of the function for the arg'th argument (starting to count at 0).
     * This is needed for SQL type casting inside KiWi.
     *
     * @param arg
     * @return
     */
    @Override
    public ValueType getArgumentType(int arg) {
        return ValueType.STRING;
    }

    /**
     * Return the minimum number of arguments this function requires.
     *
     * @return
     */
    @Override
    public int getMinArgs() {
        return 3;
    }

    /**
     * Return the maximum number of arguments this function can take
     *
     * @return
     */
    @Override
    public int getMaxArgs() {
        return 4;
    }
}
