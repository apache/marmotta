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
package org.apache.marmotta.kiwi.config;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;

/**
 * An object to hold a KiWi persistence configuration. The configuration consists of:
 * <ul>
 *     <li>a name to identify the persistence configuration throughout the system</li>
 *     <li>a JDBC URL for connecting to the respective relational database</li>
 *     <li>a database user to access the database</li>
 *     <li>a database password for that user</li>
 *     <li>a KiWi dialect for representing the different SQL dialects</li>
 * </ul>
 * <p/>
 * Author: Sebastian Schaffert
 */
public class KiWiConfiguration {

    private String name;
    private String jdbcUrl;
    private String dbUser;
    private String dbPassword;
    private KiWiDialect dialect;

    public KiWiConfiguration(String name, String jdbcUrl, String dbUser, String dbPassword, KiWiDialect dialect) {
        this.dbPassword = dbPassword;
        this.dbUser = dbUser;
        this.dialect = dialect;
        this.jdbcUrl = jdbcUrl;
        this.name = name;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbUser() {
        return dbUser;
    }

    public KiWiDialect getDialect() {
        return dialect;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getName() {
        return name;
    }
}
