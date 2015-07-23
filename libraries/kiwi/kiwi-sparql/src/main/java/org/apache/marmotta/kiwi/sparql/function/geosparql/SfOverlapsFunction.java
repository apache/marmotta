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
package org.apache.marmotta.kiwi.sparql.function.geosparql;


import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sparql.builder.ValueType;
import org.apache.marmotta.kiwi.sparql.function.NativeFunction;
import org.apache.marmotta.kiwi.vocabulary.FN_GEOSPARQL;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;

/**
 * A SPARQL function for doing a overlaps between geometries. Should be implemented directly in
 * the database, as the in-memory implementation is non-functional. Only support by postgres - POSTGIS
 * <p/>
 * The function can be called either as:
 * <ul>
 *     <li>geof:sfOverlaps(?geometryA, ?geometryB) </li>
 * </ul>
 * Its necesary enable postgis in your database with the next command "CREATE EXTENSION postgis;"
 * Note that for performance reasons it might be preferrable to create a geometry index for your database. Please
 * consult your database documentation on how to do this.
 *
 * @author Xavier Zumba (xavier.sumba93@ucuenca.ec))
 */
public class SfOverlapsFunction implements NativeFunction {

    // auto-register for SPARQL environment
    static {
        if(!FunctionRegistry.getInstance().has(FN_GEOSPARQL.SF_OVERLAPS.toString())) {
            FunctionRegistry.getInstance().add(new SfOverlapsFunction());
        }
    }

    @Override
    public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        throw new UnsupportedOperationException("cannot evaluate in-memory, needs to be supported by the database");
    }

    @Override
    public String getURI() {
        return FN_GEOSPARQL.SF_OVERLAPS.toString();
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
     * Return a string representing how this GeoSPARQL function is translated into SQL ( Postgis Function ) in the given dialect
     *
     * @param dialect
     * @param args
     * @return
     */
    @Override
    public String getNative(KiWiDialect dialect, String... args) {
        if(dialect instanceof PostgreSQLDialect) {
            if(args.length == 2) {
                if (args[1].contains(FN_GEOSPARQL.MULTIPOLYGON)|| args[1].contains(FN_GEOSPARQL.MULTILINESTRING) || args[1].contains(FN_GEOSPARQL.POINT))
                {  //If users insert Direct the WKT  Geometry 
                    return "st_Overlaps(" + args[0] + " , " + args[1] + " )";    
                }        
                return "st_Overlaps(" + args[0] + " , " + args[1] + " )";
            } 

        }
        throw new UnsupportedOperationException("sfOverlaps function not supported by dialect "+dialect);
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
        return ValueType.GEOMETRY;
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
