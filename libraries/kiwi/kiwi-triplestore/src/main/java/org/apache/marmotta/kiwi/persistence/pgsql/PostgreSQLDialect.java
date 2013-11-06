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
package org.apache.marmotta.kiwi.persistence.pgsql;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.kiwi.exception.DriverNotFoundException;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.FN;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class PostgreSQLDialect extends KiWiDialect {

    public PostgreSQLDialect() {
        super();
        try {
            Class.forName(getDriverClass());
        } catch (ClassNotFoundException e) {
            throw new DriverNotFoundException(getDriverClass());
        }

        supportedFunctions.add(FN.CONCAT);
        supportedFunctions.add(FN.CONTAINS);
        supportedFunctions.add(FN.LOWER_CASE);
        supportedFunctions.add(FN.UPPER_CASE);
        supportedFunctions.add(FN.REPLACE);
        supportedFunctions.add(FN.SUBSTRING_AFTER);
        supportedFunctions.add(FN.SUBSTRING_BEFORE);
        supportedFunctions.add(FN.STRING_LENGTH);
        supportedFunctions.add(FN.STARTS_WITH);
        supportedFunctions.add(FN.ENDS_WITH);
    }

    /**
     * Return the name of the driver class (used for properly initialising JDBC connections)
     *
     * @return
     */
    @Override
    public String getDriverClass() {
        return "org.postgresql.Driver";
    }

    @Override
    public boolean isBatchSupported() {
        return true;
    }

    @Override
    public String getRegexp(String text, String pattern, String flags) {
        StringBuilder flagList = new StringBuilder();
        if(StringUtils.containsIgnoreCase(flags,"i")) {
            flagList.append("i");
        }
        if(flagList.length() == 0) {
            return text + " ~ " + pattern;
        } else {
            return String.format("%s ~ (?%s)%s", text, flagList.toString(), pattern);
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
        if(StringUtils.containsIgnoreCase(flags, "s")) {
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
        return text + " ILIKE " + pattern;
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

    /**
     * Return true in case the database system supports using cursors for queries over large data tables.
     *
     * @return
     */
    @Override
    public boolean isCursorSupported() {
        return true;
    }


    /**
     * Return an SQL string for evaluating the function with the given URI on the arguments. All arguments are already
     * properly substituted SQL expressions (e.g. field names or constants).
     * <p/>
     * Dialects should at least implement support for all functions defined in the SPARQL specification (http://www.w3.org/TR/sparql11-query/#SparqlOps)
     *
     * @param fnUri
     * @param args
     * @return
     * @throws UnsupportedOperationException in case the function with the given URI is not supported
     */
    @Override
    public String getFunction(URI fnUri, String... args) {
        if(FN.CONCAT.equals(fnUri)) {
            return String.format("(%s)", StringUtils.join(args, "||"));
        } else if(FN.CONTAINS.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 2);
            return String.format("(position(%s in %s) > 0)", args[1], args[0]);
        } else if(FN.LOWER_CASE.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("lower(%s)", args[0]);
        } else if(FN.UPPER_CASE.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("upper(%s)", args[0]);
        } else if(FN.REPLACE.equals(fnUri)) {
            if(args.length == 3) {
                // no flags
                return String.format("regexp_replace(%s, %s, %s, 'g')", args[0], args[1], args[2]);
            } else if(args.length == 4) {
                // flags
                StringBuilder psqlFlags = new StringBuilder();
                if(StringUtils.containsIgnoreCase(args[3],"i")) {
                    psqlFlags.append("i");
                }

                return String.format("regexp_replace(%s, %s, %s, 'g%s')", args[0], args[1], args[2], psqlFlags.toString());
            } else {
                throw new IllegalArgumentException("invalid number of arguments");
            }
        } else if(FN.SUBSTRING_AFTER.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 2);
            return String.format("(CASE WHEN position(%2$s in %1$s) > 0 THEN substring(%1$s from position(%2$s in %1$s) + char_length(%2$s) ) ELSE '' END)", args[0], args[1]);
        } else if(FN.SUBSTRING_BEFORE.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 2);
            return String.format("(CASE WHEN position(%2$s in %1$s) > 0 THEN substring(%1$s from 1 for position(%2$s in %1$s)-1) ELSE '' END)", args[0], args[1]);

        } else if(FN.STRING_LENGTH.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("char_length(%s)", args[0]);
        } else if(FN.STARTS_WITH.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 2);
            return String.format("(POSITION(%2$s IN %1$s) = 1)", args[0], args[1]);
        } else if(FN.ENDS_WITH.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 2);
            return String.format("(POSITION(reverse(%2$s) IN reverse(%1$s)) = 1)", args[0], args[1]);
        }
        throw new UnsupportedOperationException("operation "+fnUri+" not supported");
    }


}
