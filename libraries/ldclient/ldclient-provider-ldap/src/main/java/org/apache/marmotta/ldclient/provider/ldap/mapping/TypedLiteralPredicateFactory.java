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
package org.apache.marmotta.ldclient.provider.ldap.mapping;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.util.Collections;
import java.util.Set;

public class TypedLiteralPredicateFactory extends LiteralPredicateFactory {

    protected final String typeURI;

    public TypedLiteralPredicateFactory(String predicateURI, String typeURI) {
        super(predicateURI);
        this.typeURI = typeURI;
    }

    public TypedLiteralPredicateFactory(String predicateURI) {
        this(predicateURI, Namespaces.NS_XSD + "string");
    }

    @Override
    public Set<Value> createObjects(String value, ValueFactory valueFactory) {
        final URI type = valueFactory.createURI(typeURI);
        return Collections.singleton((Value) valueFactory.createLiteral(value, type));
    }

}
