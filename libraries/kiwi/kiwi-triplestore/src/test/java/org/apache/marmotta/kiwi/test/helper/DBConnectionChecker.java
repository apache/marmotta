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
package org.apache.marmotta.kiwi.test.helper;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.junit.Assume;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionChecker {

	private DBConnectionChecker() {
		// static only
	}
	
	public static void checkDatabaseAvailability(String jdbcUrl, String jdbcUser,
			String jdbcPass, KiWiDialect dialect) {
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

}
