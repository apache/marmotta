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
package org.apache.marmotta.platform.core.api.user;


import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;

import java.util.Date;
import java.util.UUID;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class UserToken {

    private Date created, modified;

    private UUID uuid;

    private KiWiUriResource user;


    public UserToken(KiWiUriResource user) {
        this.user     = user;
        this.uuid     = UUID.randomUUID();
        this.created  = new Date();
        this.modified = new Date();
    }

    public Date getCreated() {
        return new Date(created.getTime());
    }

    public void setCreated(Date created) {
        this.created = new Date(created.getTime());
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public KiWiUriResource getUser() {
        return user;
    }

    public void setUser(KiWiUriResource user) {
        this.user = user;
    }

    public Date getModified() {
        return new Date(modified.getTime());
    }

    public void setModified(Date modified) {
        this.modified = new Date(modified.getTime());
    }

    @Override
    public String toString() {
        return String.format("%s; %tFT%<tTZ", user, modified);
        // return uuid.toString();
    }
}
