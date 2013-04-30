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

import org.apache.marmotta.kiwi.exception.DriverNotFoundException;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class H2Dialect extends KiWiDialect {

    public H2Dialect() {
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
    public String getRegexp(String text, String pattern) {
        return text + " REGEXP " + pattern;
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

}
