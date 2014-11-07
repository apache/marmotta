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
package org.apache.marmotta.kiwi.model.rdf;

import com.google.common.base.Preconditions;
import org.apache.marmotta.commons.sesame.model.URICommons;
import org.openrdf.model.URI;

import java.util.Date;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class KiWiUriResource extends KiWiResource implements URI {

	private static final long serialVersionUID = -6399293877969640084L;

    private String uri;


    //@Transient
    private String namespace;

    //@Transient
    private String localName;

    @Deprecated
    public KiWiUriResource() {
        super();
    }

    public KiWiUriResource(String uri) {
        super();
        Preconditions.checkArgument(uri.indexOf(':') >= 0, "Not a valid (absolute) URI: " + uri);
        this.uri = uri;
    }

    public KiWiUriResource(String uri, Date created) {
        super(created);
        Preconditions.checkArgument(uri.indexOf(':') >= 0, "Not a valid (absolute) URI: " + uri);
        this.uri = uri;
    }


    /**
     * @deprecated use {@link #stringValue()} instead.
     */
    @Deprecated
    public String getUri() {
        return uri;
    }

    @Deprecated
    public void setUri(String uri) {
        Preconditions.checkArgument(uri.indexOf(':') >= 0, "Not a valid (absolute) URI: " + uri);
        this.uri = uri;
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
     * a {@link org.openrdf.model.Literal}'s label, a {@link org.openrdf.model.URI}'s URI or a {@link org.openrdf.model.BNode}'s ID.
     */
    @Override
    public String stringValue() {
        return uri;
    }

    @Override
    public boolean isAnonymousResource() {
        return false;
    }

    @Override
    public boolean isUriResource() {
        return true;
    }


    @Override
    public String toString() {
        return uri;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if(o instanceof URI) {
            return this.stringValue().equals(((URI)o).stringValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }


    private void initNamespace() {
        if(namespace == null || localName == null) {
            String[] components = URICommons.splitNamespace(uri);
            namespace = components[0];
            localName = components[1];
        }
    }

}

