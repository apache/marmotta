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

package org.apache.marmotta.kiwi.sparql.builder;

import org.openrdf.model.URI;

/**
 * Description of a SPARQL function to give details about various characteristics that we need when translating to
 * SQL, most importantly the parameter and return types.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class FunctionDescriptor {

    public static enum Arity { ZERO, UNARY, BINARY, NARY };

    private URI uri;

    private OPTypes parameterType, returnType;

    private Arity arity;

    public FunctionDescriptor(URI uri, OPTypes parameterType, OPTypes returnType, Arity arity) {
        this.uri = uri;
        this.parameterType = parameterType;
        this.returnType = returnType;
        this.arity = arity;
    }


    public URI getUri() {
        return uri;
    }

    public OPTypes getParameterType() {
        return parameterType;
    }

    public OPTypes getReturnType() {
        return returnType;
    }

    public Arity getArity() {
        return arity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionDescriptor that = (FunctionDescriptor) o;

        if (arity != that.arity) return false;
        if (parameterType != that.parameterType) return false;
        if (returnType != that.returnType) return false;
        if (!uri.equals(that.uri)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + parameterType.hashCode();
        result = 31 * result + returnType.hashCode();
        result = 31 * result + arity.hashCode();
        return result;
    }
}
