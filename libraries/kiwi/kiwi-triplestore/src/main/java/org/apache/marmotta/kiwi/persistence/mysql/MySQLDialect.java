/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
}
