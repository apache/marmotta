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

import org.openrdf.model.Value;

import java.io.Serializable;
import java.util.Date;


/**
 * KiWiNode is the base class of all objects stored in the knowledge base. The relevant subclasses 
 * are KiWiResource and KiWiLiteral.
 * <p/>
 * The KiWiNode entity represents common properties of nodes like the creation and modification dates, the
 * creator, and the context.
 * * 
 * @author sschaffe
 */
public abstract class KiWiNode implements Value, Serializable {

    private static final long serialVersionUID = 4652575123005436645L;

    /**
     * Unique ID of the KiWiNode in the database. This ID should not be exposed to the outside, it may be
     * different between different KiWi installations.
     */
    private long id = -1L;

    /**
     * The creation date of the KiWiNode.
     **/
    private Date created;

    protected KiWiNode() {
        this(new Date());
    }

    protected KiWiNode(Date created) {
        this.created   = created;
    }

    /**
     * Return the database ID of this node. Can be used to refer to the node in the context of one triple store
     * instance, but should not be exposed outside.
     *
     * @return the (internal) database id
     */
    public long getId() {
        return id;
    }

    /**
     * Update the database id of the node.
     *
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Return the timestamp when this node has been created
     *
     * @return the created
     */
    public Date getCreated() {
        return new Date(created.getTime());
    }

    /**
     * Set the timestamp when this resource has been created
     *
     * @param created the created to set
     */
    public void setCreated(Date created) {
        this.created = new Date(created.getTime());
    }


    /**
     * Return true in case the node is a uri resource (avoids instanceof test)
     *
     * @return true if the node is a URI, false otherwise
     */
    public abstract boolean isUriResource();

    /**
     * Return true in case the node is an anonymous resource / BNode (avoids instanceof test)
     *
     * @return true if the node is a blank node, false otherwise
     */
    public abstract boolean isAnonymousResource();

    /**
     * Return true in case the node is a literal (avoids instanceof test)
     *
     * @return true in case the node is a literal, false otherwise
     */
    public abstract boolean isLiteral();


}
