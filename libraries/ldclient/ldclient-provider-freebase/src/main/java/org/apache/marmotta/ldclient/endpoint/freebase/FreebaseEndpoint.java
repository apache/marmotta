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
package org.apache.marmotta.ldclient.endpoint.freebase;

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.provider.freebase.FreebaseProvider;

/**
 * Endpoint for accessing RDF from Freebase.
 *
 * @author Sergio Fernández
 */
public class FreebaseEndpoint extends Endpoint {

    public FreebaseEndpoint() {
        super(FreebaseProvider.NAME, FreebaseProvider.NAME, FreebaseProvider.PATTERN, null, 86400L);
        setPriority(PRIORITY_MEDIUM);
        addContentType(new ContentType("text", "turtle", 1.0));
        addContentType(new ContentType("text", "plain", 0.2));
        addContentType(new ContentType("*", "*", 0.1));
    }

}
