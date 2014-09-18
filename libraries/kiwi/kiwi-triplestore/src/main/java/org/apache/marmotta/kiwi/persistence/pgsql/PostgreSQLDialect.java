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
import org.apache.marmotta.kiwi.vocabulary.FN_MARMOTTA;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class PostgreSQLDialect extends KiWiDialect {

    public PostgreSQLDialect() throws DriverNotFoundException {
        try {
            Class.forName(getDriverClass());
        } catch (ClassNotFoundException e) {
            throw new DriverNotFoundException(getDriverClass());
        }

        supportedFunctions.put(FN.CONCAT);
        supportedFunctions.put(FN.CONTAINS);
        supportedFunctions.put(FN.LOWER_CASE);
        supportedFunctions.put(FN.UPPER_CASE);
        supportedFunctions.put(FN.REPLACE);
        supportedFunctions.put(FN.SUBSTRING_AFTER);
        supportedFunctions.put(FN.SUBSTRING_BEFORE);
        supportedFunctions.put(FN.STRING_LENGTH);
        supportedFunctions.put(FN.STARTS_WITH);
        supportedFunctions.put(FN.ENDS_WITH);

        supportedFunctions.put(FN.NUMERIC_ABS);
        supportedFunctions.put(FN.NUMERIC_CEIL);
        supportedFunctions.put(FN.NUMERIC_FLOOR);
        supportedFunctions.put(FN.NUMERIC_ROUND);

        supportedFunctions.put(XMLSchema.DOUBLE);
        supportedFunctions.put(XMLSchema.FLOAT);
        supportedFunctions.put(XMLSchema.INTEGER);
        supportedFunctions.put(XMLSchema.DECIMAL);
        supportedFunctions.put(XMLSchema.DATETIME);
        supportedFunctions.put(XMLSchema.BOOLEAN);


        supportedFunctions.put(FN_MARMOTTA.SEARCH_FULLTEXT);
        supportedFunctions.put(FN_MARMOTTA.QUERY_FULLTEXT);

        supportedFunctions.put(FN_MARMOTTA.NOW);
        supportedFunctions.put(FN_MARMOTTA.YEAR);
        supportedFunctions.put(FN_MARMOTTA.MONTH);
        supportedFunctions.put(FN_MARMOTTA.DAY);
        supportedFunctions.put(FN_MARMOTTA.HOURS);
        supportedFunctions.put(FN_MARMOTTA.MINUTES);
        supportedFunctions.put(FN_MARMOTTA.SECONDS);

        supportedFunctions.put(FN_MARMOTTA.RAND);
        supportedFunctions.put(FN_MARMOTTA.UUID);
        supportedFunctions.put(FN_MARMOTTA.STRUUID);
        supportedFunctions.put(FN_MARMOTTA.MD5);
        supportedFunctions.put(FN_MARMOTTA.SHA1);
        supportedFunctions.put(FN_MARMOTTA.SHA256);
        supportedFunctions.put(FN_MARMOTTA.SHA384);
        supportedFunctions.put(FN_MARMOTTA.SHA512);

        supportedFunctions.put(FN_MARMOTTA.STDDEV);
        supportedFunctions.put(FN_MARMOTTA.VARIANCE);
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
        return "SELECT 1; COMMIT;";
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
     * Return true in case the database supports creating arrays with ARRAY[...]
     *
     * @return
     */
    @Override
    public boolean isArraySupported() {
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
        } else if(FN.NUMERIC_ABS.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("abs(%s)", args[0]);
        } else if(FN.NUMERIC_CEIL.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("ceil(%s)", args[0]);
        } else if(FN.NUMERIC_FLOOR.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("floor(%s)", args[0]);
        } else if(FN.NUMERIC_ROUND.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("round(%s)", args[0]);
        } else if(XMLSchema.DOUBLE.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("CAST(%s AS double precision)", args[0]);
        } else if(XMLSchema.FLOAT.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("CAST(%s AS double precision)", args[0]);
        } else if(XMLSchema.INTEGER.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("CAST(%s AS bigint)", args[0]);
        } else if(XMLSchema.DECIMAL.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("CAST(%s AS decimal)", args[0]);
        } else if(XMLSchema.DATETIME.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("CAST(%s AS timestamp)", args[0]);
        } else if(XMLSchema.BOOLEAN.equals(fnUri)) {
            Preconditions.checkArgument(args.length == 1);
            return String.format("CAST(%s AS boolean)", args[0]);
        } else if(FN_MARMOTTA.SEARCH_FULLTEXT.equals(fnUri)) {
            if(args.length == 2) {
                return String.format("(to_tsvector('simple' :: regconfig,%1$s) @@ plainto_tsquery('simple' :: regconfig,%2$s))", args[0], args[1]);
            } else if(args.length == 3) {
                return String.format("(to_tsvector(kiwi_ft_lang(%3$s) :: regconfig, %1$s) @@ plainto_tsquery(kiwi_ft_lang(%3$s) :: regconfig, %2$s))", args[0], args[1], args[2]);
            } else {
                throw new IllegalArgumentException("invalid number of arguments");
           }
        } else if(FN_MARMOTTA.QUERY_FULLTEXT.equals(fnUri)) {
            if(args.length == 2) {
                return String.format("(to_tsvector('simple' :: regconfig,%1$s) @@ to_tsquery('simple' :: regconfig,%2$s))", args[0], args[1]);
            } else if(args.length == 3) {
                return String.format("(to_tsvector(kiwi_ft_lang(%3$s) :: regconfig, %1$s) @@ to_tsquery(kiwi_ft_lang(%3$s) :: regconfig, %2$s))", args[0], args[1], args[2]);
            } else {
                throw new IllegalArgumentException("invalid number of arguments");
            }
        } else if(FN_MARMOTTA.NOW.equals(fnUri)) {
            if(args.length == 0) {
                return "now()";
            } else {
                throw new IllegalArgumentException("NOW() does not take arguments");
            }
        } else if(FN_MARMOTTA.RAND.equals(fnUri)) {
            if(args.length == 0) {
                return "random()";
            } else {
                throw new IllegalArgumentException("RAND() does not take arguments");
            }
        } else if(FN_MARMOTTA.UUID.equals(fnUri)) {
            if(args.length == 0) {
                return "'urn:uuid:' || gen_random_uuid()";
            } else {
                throw new IllegalArgumentException("UUID() does not take arguments");
            }
        } else if(FN_MARMOTTA.STRUUID.equals(fnUri)) {
            if(args.length == 0) {
                return "CAST(gen_random_uuid() AS text)";
            } else {
                throw new IllegalArgumentException("STRUUID() does not take arguments");
            }
        } else if(FN_MARMOTTA.MD5.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("encode(digest('%s', 'md5'), 'hex')", args[0]);
            } else {
                throw new IllegalArgumentException("MD5() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.SHA1.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("encode(digest('%s', 'sha1'), 'hex')", args[0]);
            } else {
                throw new IllegalArgumentException("SHA1() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.SHA256.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("encode(digest('%s', 'sha256'), 'hex')", args[0]);
            } else {
                throw new IllegalArgumentException("SHA256() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.SHA384.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("encode(digest('%s', 'sha384'), 'hex')", args[0]);
            } else {
                throw new IllegalArgumentException("SHA384() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.SHA512.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("encode(digest('%s', 'sha512'), 'hex')", args[0]);
            } else {
                throw new IllegalArgumentException("SHA512() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.YEAR.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("extract(year from %s)", args[0]);
            } else {
                throw new IllegalArgumentException("YEAR() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.MONTH.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("extract(month from %s)", args[0]);
            } else {
                throw new IllegalArgumentException("MONTH() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.DAY.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("extract(day from %s)", args[0]);
            } else {
                throw new IllegalArgumentException("DAY() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.HOURS.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("extract(hour from %s)", args[0]);
            } else {
                throw new IllegalArgumentException("HOURS() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.MINUTES.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("extract(minute from %s)", args[0]);
            } else {
                throw new IllegalArgumentException("MINUTES() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.SECONDS.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("extract(second from %s)", args[0]);
            } else {
                throw new IllegalArgumentException("SECONDS() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.STDDEV.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("stddev(%s)", args[0]);
            } else {
                throw new IllegalArgumentException("STDDEV() takes exactly 1 argument");
            }
        } else if(FN_MARMOTTA.VARIANCE.equals(fnUri)) {
            if(args.length == 1) {
                return String.format("variance(%s)", args[0]);
            } else {
                throw new IllegalArgumentException("VARIANCE() takes exactly 1 argument");
            }
        }
        throw new UnsupportedOperationException("operation "+fnUri+" not supported");
    }


}
