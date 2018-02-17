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

package org.apache.marmotta.ostrich.model;


import org.apache.marmotta.commons.sesame.model.IRICommons;
import org.apache.marmotta.ostrich.model.proto.Model;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

/**
 * An implementation of a Sesame URI backed by a protocol buffer.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ProtoIRI implements IRI {

    private Model.URI message;

    private String namespace, localName;


    public ProtoIRI(String uri) {
        message = Model.URI.newBuilder().setUri(uri).build();
    }

    public ProtoIRI(Model.URI message) {
        this.message = message;
    }

    public Model.URI getMessage() {
        return message;
    }

    /**
     * Gets the local name of this URI. The local name is defined as per the
     * algorithm described in the class documentation.
     *
     * @return The URI's local name.
     */
    @Override
    public String getLocalName() {
        initNamespace();

        return localName;
    }

    /**
     * Gets the namespace of this URI. The namespace is defined as per the
     * algorithm described in the class documentation.
     *
     * @return The URI's namespace.
     */
    @Override
    public String getNamespace() {
        initNamespace();

        return namespace;
    }

    /**
     * Returns the String-value of a <tt>Value</tt> object. This returns either
     * a {@link Literal}'s label, a {@link URI}'s URI or a {@link BNode}'s ID.
     */
    @Override
    public String stringValue() {
        return message.getUri();
    }

    @Override
    public String toString() {
        return message.getUri();
    }

    private void initNamespace() {
        if(namespace == null || localName == null) {
            String[] components = IRICommons.splitNamespace(message.getUri());
            namespace = components[0];
            localName = components[1];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if(o instanceof IRI) {
            return this.stringValue().equals(((IRI)o).stringValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return stringValue().hashCode();
    }
}
