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

import org.openrdf.model.BNode;

import java.util.Date;

/**
 * The KiWiAnonResaource represents the anonymous RDF resource.
 * 
 * @author Sebastian Schaffert
 */
public class KiWiAnonResource extends KiWiResource implements BNode {

    private static final long serialVersionUID = -873594698794527452L;

    private String anonId;

    public KiWiAnonResource() {
        super();
    }
    
    public KiWiAnonResource(String id) {
        super();
        this.anonId = id;
    }

    public KiWiAnonResource(String id, Date created) {
        super(created);
        this.anonId = id;
    }


    @Deprecated
    public String getAnonId() {
        return anonId;
    }

    @Deprecated
    public void setAnonId(String id) {
        this.anonId = id;
    }

    /**
     * retrieves this blank node's identifier.
     *
     * @return A blank node identifier.
     */
    @Override
    public String getID() {
        return anonId;
    }

    /**
     * Returns the String-value of a <tt>Value</tt> object. This returns either
     * a {@link org.openrdf.model.Literal}'s label, a {@link org.openrdf.model.URI}'s URI or a {@link org.openrdf.model.BNode}'s ID.
     */
    @Override
    public String stringValue() {
        return anonId;
    }

    @Override
    public String toString() {
        return "_:"+anonId;
    }

    @Override
	public boolean isAnonymousResource() {
		return true;
	}

	@Override
	public boolean isLiteral() {
		return false;
	}

	@Override
	public boolean isUriResource() {
		return false;
	}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if(o instanceof BNode) {
            return this.stringValue().equals(((BNode)o).stringValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return anonId.hashCode();
    }
}
