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
package org.apache.marmotta.kiwi.sparql.function.custom;

import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sparql.builder.ValueType;
import org.apache.marmotta.kiwi.sparql.function.NativeFunction;
import org.apache.marmotta.kiwi.vocabulary.FN_MARMOTTA;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * A SPARQL function for doing a full-text search on the content of a string using a query language with boolean operators.
 * The query syntax is the syntax of PostgreSQL (see http://www.postgresql.org/docs/9.1/static/datatype-textsearch.html)
 * Should be implemented directly in the database, as the in-memory implementation is non-functional.
 * <p/>
 * The function can be called either as:
 * <ul>
 *     <li>fn:fulltext-query(?var, 'query') - using a generic stemmer and dictionary</li>
 *     <li>
 *         fn:fulltext-query(?var, 'query', 'language') - using a language-specific stemmer and dictionary
 *         (currently only supported by PostgreSQL with the language values 'english', 'german', 'french', 'italian', 'spanish'
 *         and some other languages as supported by PostgreSQL).
 *     </li>*
 * </ul>
 * Note that for performance reasons it might be preferrable to create a full-text index for your database. Please
 * consult your database documentation on how to do this.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class FulltextQueryFunction implements NativeFunction {

    @Override
    public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        throw new UnsupportedOperationException("cannot evaluate in-memory, needs to be supported by the database");
    }

    @Override
    public String getURI() {
        return FN_MARMOTTA.QUERY_FULLTEXT.toString();
    }


    /**
     * Return true if this function has available native support for the given dialect
     *
     * @param dialect
     * @return
     */
    @Override
    public boolean isSupported(KiWiDialect dialect) {
        return dialect instanceof PostgreSQLDialect;
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
        if(dialect instanceof PostgreSQLDialect) {
            if(args.length == 2) {
                return String.format("(to_tsvector('simple' :: regconfig,%1$s) @@ to_tsquery('simple' :: regconfig,%2$s))", args[0], args[1]);
            } else if(args.length == 3) {
                return String.format("(to_tsvector(kiwi_ft_lang(%3$s) :: regconfig, %1$s) @@ to_tsquery(kiwi_ft_lang(%3$s) :: regconfig, %2$s))", args[0], args[1], args[2]);
            }
        }
        throw new UnsupportedOperationException("fulltext search not supported by dialect "+dialect);
    }

    /**
     * Get the return type of the function. This is needed for SQL type casting inside KiWi.
     *
     * @return
     */
    @Override
    public ValueType getReturnType() {
        return ValueType.BOOL;
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
        return 2;
    }

    /**
     * Return the maximum number of arguments this function can take
     *
     * @return
     */
    @Override
    public int getMaxArgs() {
        return 3;
    }
}
