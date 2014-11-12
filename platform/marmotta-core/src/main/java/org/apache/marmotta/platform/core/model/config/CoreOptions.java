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

package org.apache.marmotta.platform.core.model.config;

/**
 * Class with static constants for configuration options
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CoreOptions {
    public static final String BASE_URI = "kiwi.context";
    public static final String SERVER_URI = "kiwi.host";

    public static final String CACHING_EXPIRATION = "caching.expiration";
    public static final String CACHING_MAXIMUM_SIZE = "caching.maximum_size";

    // HTTP connection service
    public static final String HTTP_MAX_CONNECTIONS = "core.http.max_connections";
    public static final String HTTP_MAX_CONNECTIONS_PER_ROUTE = "core.http.max_connections_per_route";
    public static final String HTTP_CLIENT_CACHE_ENABLE = "core.http.client_cache_enable";

    // webservices
    public static final String HTTP_ALLOW_ORIGIN = "kiwi.allow_origin";
    public static final String LINKEDDATA_REDIRECT_PUT = "linkeddata.redirect.put";
    public static final String LINKEDDATA_REDIRECT_STATUS = "linkeddata.redirect.status";
    public static final String LINKEDDATA_MIME_REL_DEFAULT = "linkeddata.mime.rel.default";
}
