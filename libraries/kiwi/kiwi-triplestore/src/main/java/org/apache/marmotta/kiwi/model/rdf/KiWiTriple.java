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

import org.openrdf.model.Statement;

import java.io.Serializable;
import java.util.Date;

/**
 * KiWiTriples are one of the core concepts of the KiWi system. They
 * correspond to triples in RDF, but extend them with additional information
 * that is useful and/or needed in the KiWi system, for example author information,
 * version information, justifications (in case of inferred triples), etc.
 * 
 * Like a KiWiResource, each triple is associated with a single TripleStore.
 * All triples together essentially make up the TripleStore. As with KiWiResources,
 * this means that there is no 1:1 correspondence between RDF triples and KiWi triples.
 * An RDF triple may be represented by several KiWi triples in different KnowledgeSpaces.
 * <p>
 * Base/inferred flags denote the status of support of a triple:
 * <ul>
 * <li>base=true, inferred=true means  the triple is both explicit and supported by a rule</li>
 * <li>base=false, inferred=true means triple is supported by a rule but not explicit</li>
 * <li>base=true, inferred=false means triple is only explicit</li>
 * <li>base=false, inferred=false does not exist</li>
 * </ul>
 * Only triples where base=true can be deleted using TripleStore.removeTriple; triples with
 * base=false and inferred=true are managed by the reasoner.
 *
 * @author Sebastian Schaffert
 */
public class KiWiTriple  implements Statement, Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -8726615974625660845L;
	
	private Long id;
	

    private KiWiResource    subject;
    
    private KiWiUriResource predicate;
	
    private KiWiNode        object;

    private KiWiResource context;
    
    private KiWiResource creator;
	
	private Date created;

    private Date deletedAt;

	private Boolean deleted;

    private Boolean inferred;

    //@Transient
    private Boolean markedForReasoning;

	
	public KiWiTriple() {
        this(new Date());
	}

    public KiWiTriple(Date created) {
        this.created = created;
        this.deleted = false;
        this.inferred = false;
        this.markedForReasoning = false;
        this.deletedAt = null;
    }


	public KiWiTriple(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context) {
		this(subject, predicate, object, context, new Date());
	}


    public KiWiTriple(KiWiResource subject, KiWiUriResource predicate, KiWiNode object, KiWiResource context, Date created) {
        this(created);
        this.subject = subject;
        this.predicate = predicate;
        this.object   = object;
        this.context  = context;
        this.deletedAt = null;

        assert(subject  != null);
        assert(predicate != null);
        assert(object   != null);
    }

    /**
     * Get the object of this extended triple.
     * @return
     */
    public KiWiNode getObject() {
        return object;
    }

    /**
     * Set the object of this extended triple to the given KiWiNode (either a resource or a literal)
     * @param object
     */
    public void setObject(KiWiNode object) {
        this.object = object;
    }

    /**
     * Get the property of this extended triple. Always a KiWiUriResource.
     * @return
     */
    public KiWiUriResource getPredicate() {
        return predicate;
    }

    /**
     * Set the property of this extended triple. Always needs to be a KiWiUriResource
     * @param property
     */
    public void setPredicate(KiWiUriResource property) {
        this.predicate = property;
    }

    /**
     * Get the subject of this extended triple. Always a resource.
     * @return
     */
    public KiWiResource getSubject() {
        return subject;
    }

    /**
     * Set the subject of this extended triple to the provided KiWiResource
     * @param subject
     */
    public void setSubject(KiWiResource subject) {
        this.subject = subject;
    }

    /**
     * Get the unique triple identifier of this extended triple. Returns a KiWiUriResource identifying this triple.
     * @return
     */
    public KiWiResource getContext() {
        return context;
    }

    /**
     * Set the unique triple identifier of this extended triple to the provided KiWiUriResource. The caller needs
     * to ensure that the tripleId is unique over the KiWi system; otherwise, the system might not function correctly.
     * @param context
     */
    public void setContext(KiWiResource context) {
        this.context = context;
    }

    
    /**
     * Return the author of this extended triple.
     * 
     * Internally, this is determined using the tripleId of the extended triple and looking it up in the 
     * database.
     * 
     * @return
     */
    public KiWiResource getCreator() {
        return creator;
    }
    
    /**
     * Set the author of this extended triple.
     * 
     * Changes will be persisted as part of the database using the tripleId as unique identifier.
     * 
     * @param author
     */
    public void setCreator(KiWiResource author) {
        this.creator = author;
    }

    
    
	/**
	 * @return the created
	 */
	public Date getCreated() {
		return new Date(created.getTime());
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(Date created) {
		this.created = new Date(created.getTime());
	}

    /**
     * Return the date the triple has been deleted, or null in case the triple is not deleted
     *
     * @return
     */
    public Date getDeletedAt() {
        return deletedAt == null ? null : new Date(deletedAt.getTime());
    }

    /**
     * Set the date the triple has been deleted.
     *
     * @param deletedAt
     */
    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt != null ? new Date(deletedAt.getTime()) : null;
    }

    /**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	
	
	/**
	 * @return the deleted
	 */
	public Boolean isDeleted() {
		return deleted;
	}


	/**
	 * @param deleted the deleted to set
	 */
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        Statement triple = (Statement) o;
//        changed according to https://openrdf.atlassian.net/browse/SES-1924
//        if (!getContext().equals(triple.getContext())) return false;
        if (!getObject().equals(triple.getObject())) return false;
        if (!getPredicate().equals(triple.getPredicate())) return false;
        return getSubject().equals(triple.getSubject());

    }

    @Override
    public int hashCode() {
        return 961 * getSubject().hashCode() + 31 * getPredicate().hashCode() + getObject().hashCode();
    }


    @Override
    public String toString() {
    	if(context != null) {
    		return "{"+subject.toString()+" "+ predicate.toString()+" "+object.toString()+"}@"+context.toString();
    	} else {
    		return "{"+subject.toString()+" "+ predicate.toString()+" "+object.toString()+"}@GLOBAL";   		
    	}
    }
    
    /**
     * Return a unique key to be used in caches and similar.
     * 
     * @return
     */
    public String getKey() {
    	return toString();
    }



    public boolean isInferred() {
        return inferred;
    }

    public void setInferred(boolean inferred) {
        this.inferred = inferred;
    }


    public Boolean isMarkedForReasoning() {
        return markedForReasoning;
    }

    public void setMarkedForReasoning(Boolean markedForReasoning) {
        this.markedForReasoning = markedForReasoning;
    }
}
