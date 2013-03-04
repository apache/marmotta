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
package org.apache.marmotta.ldcache.model;

import org.openrdf.model.URI;

import java.util.Date;

/**
 * The cache entry for a URI resource managed by the Linked Data Cache. Contains maintenance information about
 * the resource, i.e. when it has been retrieved last, when to retrieve it next, etc.
 * <p/>
 * User: Sebastian Schaffert
 */
public class CacheEntry {

    /**
     * The URI resource managed by this cache entry.
     */
    private URI resource;

    /**
     * The date when this resource has been retrieved the last time.
     */
    private Date lastRetrieved;


    /**
     * The date when this resource needs to be retrieved again according to expiry configuration.
     */
    private Date expiryDate;


    /**
     * The number of times this resource has already been updated.
     */
    private Integer updateCount;



    public CacheEntry() {
    }


    public URI getResource() {
        return resource;
    }

    public void setResource(URI resource) {
        this.resource = resource;
    }

    public Date getLastRetrieved() {
        return lastRetrieved;
    }

    public void setLastRetrieved(Date lastRetrieved) {
        this.lastRetrieved = new Date(lastRetrieved.getTime());
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = new Date(expiryDate.getTime());
    }

    public Integer getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Integer updateCount) {
        this.updateCount = updateCount;
    }

}
