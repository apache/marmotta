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

package org.apache.marmotta.kiwi.sparql.builder.collect;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.kiwi.sparql.builder.ValueType;
import org.apache.marmotta.kiwi.sparql.function.NativeFunction;
import org.apache.marmotta.kiwi.sparql.function.NativeFunctionRegistry;
import org.openrdf.model.Literal;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Determine the operand type of a value expression. Get the coerced value by calling coerce().
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class OPTypeFinder extends QueryModelVisitorBase<RuntimeException> {

    public List<ValueType> optypes = new ArrayList<>();



    public OPTypeFinder(ValueExpr expr) {
        expr.visit(this);
    }

    @Override
    public void meet(ValueConstant node) throws RuntimeException {
        if(node.getValue() instanceof Literal) {
            Literal l = (Literal)node.getValue();
            String type = l.getDatatype() != null ? l.getDatatype().stringValue() : null;

            if(StringUtils.equals(Namespaces.NS_XSD + "double", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "float", type)) {
                optypes.add(ValueType.DOUBLE);
            } else if(StringUtils.equals(Namespaces.NS_XSD + "decimal", type)) {
                optypes.add(ValueType.DECIMAL);
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
                optypes.add(ValueType.INT);
            } else if(StringUtils.equals(Namespaces.NS_XSD + "dateTime", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "date", type)
                    || StringUtils.equals(Namespaces.NS_XSD + "time", type)) {
                optypes.add(ValueType.DATE);
            } else {
                optypes.add(ValueType.NODE);
            }
        } else {
            optypes.add(ValueType.STRING);
        }
    }

    @Override
    public void meet(SameTerm node) throws RuntimeException {
        optypes.add(ValueType.BOOL);
    }


    @Override
    public void meet(Str node) throws RuntimeException {
        optypes.add(ValueType.STRING);
    }

    @Override
    public void meet(Lang node) throws RuntimeException {
        optypes.add(ValueType.STRING);
    }

    @Override
    public void meet(LocalName node) throws RuntimeException {
        optypes.add(ValueType.STRING);
    }

    @Override
    public void meet(Label node) throws RuntimeException {
        optypes.add(ValueType.STRING);
    }


    @Override
    public void meet(FunctionCall fc) throws RuntimeException {
        NativeFunction nf = NativeFunctionRegistry.getInstance().get(fc.getURI());

        if (nf != null) {
            optypes.add(nf.getReturnType());
        }
    }

    @Override
    public void meet(If node) throws RuntimeException {
        node.getResult().visit(this);
        node.getAlternative().visit(this);
    }

    public ValueType coerce() {
        ValueType left = ValueType.NODE;

        for(ValueType right : optypes) {
            if(left == ValueType.NODE) {
                left = right;
            } else if(right == ValueType.NODE) {
                // keep left
            } else if(left == right) {
                // keep left
            } else if(left.isNumeric() && right.isNumeric()) {
                left =  left.compareTo(right) < 0 ? left : right;
            } else if( (left == ValueType.STRING) || (right == ValueType.STRING)) {
                left = ValueType.STRING;
            } else {
                throw new IllegalArgumentException("unsupported type coercion: " + left + " and " + right);
            }
        }
        return left;
    }


}
