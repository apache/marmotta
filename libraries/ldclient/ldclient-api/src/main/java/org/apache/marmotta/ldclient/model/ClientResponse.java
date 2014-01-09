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
package org.apache.marmotta.ldclient.model;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.openrdf.model.Model;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

import java.util.Date;

/**
 * LDCache Client Response
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class ClientResponse {

    private static final int DEFAULT_HTTP_CODE = 200;

    private static final int DEFAULT_EXPIRATION_IN_DAYS = 7;

    /**
     * The HTTP status code returned by the client
     */
    private int httpStatus;

    private Model data;

    private Date expires;

    @Deprecated
    public ClientResponse(int httpStatus, Repository triples) {
        this.expires = DateUtils.addDays(new Date(), DEFAULT_EXPIRATION_IN_DAYS);
        this.httpStatus = httpStatus;

        try {
        this.data = ModelCommons.asModel(triples);
        } catch (RepositoryException e) {
            this.data = new TreeModel();
        }
    }

    public ClientResponse(int httpStatus, Model triples) {
        this.data = triples;
        this.expires = DateUtils.addDays(new Date(), DEFAULT_EXPIRATION_IN_DAYS);
        this.httpStatus = httpStatus;
    }


    @Deprecated
    public Repository getTriples() {
        try {
            return ModelCommons.asRepository(this.data);
        } catch (RepositoryException e) {
            return null;
        }
    }

    public Model getData() {
        return data;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = new Date(expires.getTime());
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
}
