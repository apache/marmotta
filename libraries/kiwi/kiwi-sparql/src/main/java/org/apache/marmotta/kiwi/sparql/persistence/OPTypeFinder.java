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

package org.apache.marmotta.kiwi.sparql.persistence;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Determine the operand type of a value expression. Get the coerced value by calling coerce().
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class OPTypeFinder extends QueryModelVisitorBase<RuntimeException> {

    List<OPTypes> optypes = new ArrayList<>();

    private static Map<URI,OPTypes> functionReturnTypes = new HashMap<>();
    static {
        functionReturnTypes.put(FN.CONCAT, OPTypes.STRING);
        functionReturnTypes.put(FN.CONTAINS, OPTypes.BOOL);
        functionReturnTypes.put(FN.LOWER_CASE, OPTypes.STRING);
        functionReturnTypes.put(FN.UPPER_CASE, OPTypes.STRING);
        functionReturnTypes.put(FN.REPLACE, OPTypes.STRING);
        functionReturnTypes.put(FN.SUBSTRING_AFTER, OPTypes.STRING);
        functionReturnTypes.put(FN.SUBSTRING_BEFORE, OPTypes.STRING);
        functionReturnTypes.put(FN.STARTS_WITH, OPTypes.BOOL);
        functionReturnTypes.put(FN.ENDS_WITH, OPTypes.BOOL);
        functionReturnTypes.put(FN.STRING_LENGTH, OPTypes.INT);
        functionReturnTypes.put(FN.SUBSTRING, OPTypes.STRING);

        functionReturnTypes.put(FN.NUMERIC_ABS, OPTypes.DOUBLE);
        functionReturnTypes.put(FN.NUMERIC_CEIL, OPTypes.INT);
        functionReturnTypes.put(FN.NUMERIC_FLOOR, OPTypes.INT);
        functionReturnTypes.put(FN.NUMERIC_ROUND, OPTypes.INT);

    }



    public OPTypeFinder(ValueExpr expr) {
        expr.visit(this);
    }

    @Override
    public void meet(ValueConstant node) throws RuntimeException {
        if(node.getValue() instanceof Literal) {
            Literal l = (Literal)node.getValue();
            String type = l.getDatatype() != null ? l.getDatatype().stringValue() : null;

            if(StringUtils.equals(Namespaces.NS_XSD + "double", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "float", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "decimal", type)) {
                optypes.add(OPTypes.DOUBLE);
            } else if(StringUtils.equals(Namespaces.NS_XSD + "integer", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "long", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "int", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "short", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "nonNegativeInteger", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "nonPositiveInteger", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "negativeInteger", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "positiveInteger", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "unsignedLong", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "unsignedShort", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "byte", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "unsignedByte", type)) {
                optypes.add(OPTypes.INT);
            } else if(StringUtils.equals(Namespaces.NS_XSD + "dateTime", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "date", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "time", type)) {
                optypes.add(OPTypes.DATE);
            } else {
                optypes.add(OPTypes.STRING);
            }
        } else {
            optypes.add(OPTypes.STRING);
        }
    }

    @Override
    public void meet(Str node) throws RuntimeException {
        optypes.add(OPTypes.STRING);
    }

    @Override
    public void meet(Lang node) throws RuntimeException {
        optypes.add(OPTypes.STRING);
    }

    @Override
    public void meet(LocalName node) throws RuntimeException {
        optypes.add(OPTypes.STRING);
    }

    @Override
    public void meet(Label node) throws RuntimeException {
        optypes.add(OPTypes.STRING);
    }


    @Override
    public void meet(FunctionCall fc) throws RuntimeException {
        URI fnUri = new URIImpl(fc.getURI());

        String[] args = new String[fc.getArgs().size()];

        OPTypes fOpType = functionReturnTypes.get(fnUri);
        if(fOpType == null) {
            fOpType = OPTypes.ANY;
        }
        optypes.add(fOpType);
    }


    public OPTypes coerce() {
        OPTypes left = OPTypes.ANY;

        for(OPTypes right : optypes) {
            if(left == OPTypes.ANY) {
                left = right;
            } else if(right == OPTypes.ANY) {
                // keep left
            } else if(left == right) {
                // keep left
            } else if( (left == OPTypes.INT && right == OPTypes.DOUBLE) || (left == OPTypes.DOUBLE && right == OPTypes.INT)) {
                left = OPTypes.DOUBLE;
            } else if( (left == OPTypes.STRING) || (right == OPTypes.STRING)) {
                left = OPTypes.STRING;
            } else {
                throw new IllegalArgumentException("unsupported type coercion: " + left + " and " + right);
            }
        }
        return left;
    }


}
