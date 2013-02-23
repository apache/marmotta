/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.ldclient.model;

import org.openrdf.repository.Repository;

import java.util.Date;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class ClientResponse {

    private Repository triples;

    private Date expires;

    public ClientResponse(Repository triples) {
        this.triples = triples;
    }

    public Repository getTriples() {
        return triples;
    }

    public void setTriples(Repository triples) {
        this.triples = triples;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
