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

import org.openrdf.model.Namespace;

import java.io.Serializable;
import java.util.Date;

/**
 * The KiWiNamespace is used to store namespaces from RDF 
 * documents and link them with their prefix
 *
 * @author Sebastian Schaffert
 */
public class KiWiNamespace implements Namespace, Serializable {

	private static final long serialVersionUID = 4894726437788578181L;

	private long id = -1L;
	
    private String prefix;
    
    private String uri;

    private Date created;

    private Date modified;
    
	private boolean deleted;
    
    public KiWiNamespace() {
        this.created = new Date();
    }
    
    public KiWiNamespace(String prefix, String uri) {
    	this.deleted = false;
        this.prefix = prefix;
        this.uri = uri;
        this.created = new Date();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
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
	 * @return the modified
	 */
	public Date getModified() {
		return new Date(modified.getTime());
	}

	/**
	 * @param modified the modified to set
	 */
	public void setModified(Date modified) {
		this.modified = new Date(modified.getTime());
	}	

    /**
	 * @param deleted the deleted to set
	 */
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * @return the deleted
	 */
	public Boolean getDeleted() {
		return deleted;
	}

    /**
     * Gets the name of the current namespace (i.e. it's URI).
     *
     * @return name of namespace
     */
    @Override
    public String getName() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KiWiNamespace that = (KiWiNamespace) o;

        if (deleted != that.deleted) return false;
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = prefix != null ? prefix.hashCode() : 0;
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (deleted ? 1 : 0);
        return result;
    }

	@Override
	public int compareTo(Namespace other) {
		return uri.compareTo(other.getName());
	}
	
}
