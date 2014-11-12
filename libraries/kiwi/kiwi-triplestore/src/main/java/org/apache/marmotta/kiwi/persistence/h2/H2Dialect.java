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
package org.apache.marmotta.kiwi.persistence.h2;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.kiwi.exception.DriverNotFoundException;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class H2Dialect extends KiWiDialect {

    public H2Dialect() throws DriverNotFoundException {
        try {
            Class.forName(getDriverClass());
        } catch (ClassNotFoundException e) {
            throw new DriverNotFoundException(getDriverClass());
        }
    }

    /**
     * Return the name of the driver class (used for properly initialising JDBC connections)
     *
     * @return
     */
    @Override
    public String getDriverClass() {
        return "org.h2.Driver";
    }

    @Override
    public boolean isBatchSupported() {
        return false;
    }

    @Override
    public String getRegexp(String text, String pattern, String flags) {
        if(StringUtils.containsIgnoreCase(flags, "i")) {
            return String.format("lower(%s) REGEXP lower(%s)",text,pattern);
        } else {
            return text + " REGEXP " + pattern;
        }
    }

    /**
     * Return true in case the SPARQL RE flags contained in the given string are supported.
     *
     * @param flags
     * @return
     */
    @Override
    public boolean isRegexpSupported(String flags) {
        if(StringUtils.containsIgnoreCase(flags,"s")) {
            return false;
        }
        if(StringUtils.containsIgnoreCase(flags,"m")) {
            return false;
        }
        if(StringUtils.containsIgnoreCase(flags,"x")) {
            return false;
        }

        return true;
    }

    @Override
    public String getILike(String text, String pattern) {
        return "lower("+text+") LIKE lower("+pattern+")";
    }

    /**
     * Return the name of the aggregate function for group concatenation (string_agg in postgres, GROUP_CONCAT in MySQL)
     *
     * @param value
     * @param separator
     * @return
     */
    @Override
    public String getGroupConcat(String value, String separator, boolean distinct) {
        if(distinct) {
            value = "DISTINCT " + value;
        }
        if(separator != null) {
            return String.format("GROUP_CONCAT(%s SEPARATOR %s)", value, separator);
        } else {
            return String.format("GROUP_CONCAT(%s)", value);
        }
    }

    /**
     * Return the SQL timezone value for a KiWiDateLiteral, corrected by the timezone offset. In PostgreSQL, this is
     * e.g. computed by (ALIAS.tvalue + ALIAS.tzoffset * INTERVAL '1 second')
     *
     * @param alias
     * @return
     */
    @Override
    public String getDateTimeTZ(String alias) {
        return String.format("DATEADD('SECOND', %s.tzoffset, %s.tvalue)", alias, alias);
    }


    /**
     * Get the query string that can be used for validating that a JDBC connection to this database is still valid.
     * Typically, this should be an inexpensive operation like "SELECT 1",
     *
     * @return
     */
    @Override
    public String getValidationQuery() {
        return "SELECT 1";
    }

}
