/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 4652575123005436645L;

    /**
     * Unique ID of the KiWiNode in the database. This ID should not be exposed to the outside, it may be
     * different between different KiWi installations.
     */
	private Long id;


    /**
     * The creation date of the KiWiNode.
     **/
     private Date created;

    /**
     * indicates that the node has been marked as deleted (and can possibly be completely removed from the database)
     */
    private boolean deleted;

    /**
     * indicates that the node is cached by the Linked Data Caching service and not local
     */
    private boolean cached;
    
    /**
     * A flag to indicate whether the state of a node has changed and needs to be persisted in the database. Set by
     * createResource/createLiteral/... and used by TripleStorePersister.
     */
    // @Transient
    private boolean changed = false;

    protected KiWiNode() {
        this.created   = new Date();
        this.deleted   = false;
        this.cached    = false;
    }


    /**
     * Return the database ID of this node. Can be used to refer to the node in the context of one triple store
     * instance, but should not be exposed outside.
     *
	 * @return the (internal) database id
	 */
	public Long getId() {
		return id;
	}


	/**
     * Update the database id of the node.
     *
	 * @param id the id to set
	 */
	public void setId(Long id) {
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
     * Returns true in case the node has been marked deleted.
     *
     * @return the deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Flag this node as deleted
     *
     * @param deleted the deleted to set
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }


    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }


    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
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
