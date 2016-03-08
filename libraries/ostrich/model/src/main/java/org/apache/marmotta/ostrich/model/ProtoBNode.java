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

import org.apache.marmotta.ostrich.model.proto.Model;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;

/**
 * An implementation of a Sesame BNode backed by a protocol buffer.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ProtoBNode implements BNode {

    private Model.BNode message;

    public ProtoBNode(String id) {
        message = Model.BNode.newBuilder().setId(id).build();
    }

    public ProtoBNode(Model.BNode message) {
        this.message = message;
    }

    public Model.BNode getMessage() {
        return message;
    }

    /**
     * retrieves this blank node's identifier.
     *
     * @return A blank node identifier.
     */
    @Override
    public String getID() {
        return message.getId();
    }

    /**
     * Returns the String-value of a <tt>Value</tt> object. This returns either
     * a {@link Literal}'s label, a {@link URI}'s URI or a {@link BNode}'s ID.
     */
    @Override
    public String stringValue() {
        return message.getId();
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
        return stringValue().hashCode();
    }
}
