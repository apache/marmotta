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
package org.apache.marmotta.kiwi.persistence.mysql;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.kiwi.exception.DriverNotFoundException;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;

/**
 * A dialect for MySQL. When using MySQL, make sure the JDBC connection URL has the following arguments (workarounds
 * for non-standard MySQL behaviour):
 * <ul>
 *     <li>characterEncoding=utf8</li>
 *     <li>zeroDateTimeBehavior=convertToNull</li>
 * </ul>
 * Otherwise MySQL will not behave correctly. For example use a connection URL like:
 * <code>
 * jdbc:mysql://localhost:3306/kiwitest?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull
 * </code>

 * <p/>
 * Author: Sebastian Schaffert
 */
public class MySQLDialect extends KiWiDialect {


    public MySQLDialect() {
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
        return "com.mysql.jdbc.Driver";
    }

    @Override
    public boolean isBatchSupported() {
        return true;
    }

    @Override
    public String getRegexp(String text, String pattern, String flags) {
        if(StringUtils.containsIgnoreCase(flags,"i")) {
            return String.format("lower(%s) RLIKE lower(%s)",text,pattern);
        } else {
            return text + " RLIKE " + pattern;
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


    @Override
    public String getConcat(String... args) {
        StringBuilder buf = new StringBuilder();
        buf.append("CONCAT(");
        for(int i=0; i<args.length; i++) {
            buf.append(args[i]);
            if(i + 1 <args.length) {
                buf.append(",");
            }
        }
        buf.append(")");
        return buf.toString();
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
