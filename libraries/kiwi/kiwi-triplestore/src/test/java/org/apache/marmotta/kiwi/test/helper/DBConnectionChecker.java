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
package org.apache.marmotta.kiwi.test.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.junit.Assume;
import org.junit.internal.AssumptionViolatedException;

public class DBConnectionChecker {

    private DBConnectionChecker() {
        // static only
    }

    /**
     * Check availability of the Database.
     * @param jdbcUrl - the jdbcURL
     * @param jdbcUser - the user
     * @param jdbcPass - the password
     * @param dialect - the {@link KiWiDialect}
     * @throws AssumptionViolatedException if the database is not available.
     */
    public static void checkDatabaseAvailability(String jdbcUrl, String jdbcUser,
            String jdbcPass, KiWiDialect dialect) throws AssumptionViolatedException {
        try {
            Class.forName(dialect.getDriverClass());
            Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
            conn.setAutoCommit(false);
            Assume.assumeTrue("Database not available", conn.isValid(1000));
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            Assume.assumeNoException("Database not available", e);
        } catch (ClassNotFoundException e) {
            Assume.assumeNoException("Missing DB driver", e);
        }
    }

    /**
     * Check availability of the Database.
     * @param config the {@link KiWiConfiguration} to test
     * @throws AssumptionViolatedException if the database is not available.
     */
    public static void checkDatabaseAvailability(KiWiConfiguration config) throws AssumptionViolatedException {
        checkDatabaseAvailability(config.getJdbcUrl(), config.getDbUser(), config.getDbPassword(), config.getDialect());
    }

    /**
     * Check the availability of the Database.
     * @param jdbcUrl - the jdbcURL
     * @param jdbcUser - the user
     * @param jdbcPass - the password
     * @param dialect - the {@link KiWiDialect}
     * @return {@code true} if the database is available, {@code false} if not
     */
    public static boolean isDatabaseAvailable(String jdbcUrl, String jdbcUser,
            String jdbcPass, KiWiDialect dialect) {
        try {
            checkDatabaseAvailability(jdbcUrl, jdbcUser, jdbcPass, dialect);
            return true;
        } catch (AssumptionViolatedException ave) {
            return false;
        }
    }

    /**
     * Check availability of the Database.
     * @param config the {@link KiWiConfiguration} to test
     * @return {@code true} if the database is available, {@code false} if not
     */
    public static boolean isDatabaseAvailable(KiWiConfiguration config) {
        try {
            checkDatabaseAvailability(config);
            return true;
        } catch (AssumptionViolatedException ave) {
            return false;
        }
    }

}
