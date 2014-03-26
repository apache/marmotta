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

package org.apache.marmotta.platform.backend.kiwi;

/**
 * Class with static constants for configuration options.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiOptions {
    public static final String SPARQL_STRATEGY    = "sparql.strategy";
    public static final String DATACENTER_ID      = "database.datacenter.id";
    public static final String FULLTEXT_ENABLED   = "database.fulltext.enabled";
    public static final String FULLTEXT_LANGUAGES = "database.fulltext.languages";
    public static final String DEBUG_SLOWQUERIES = "database.debug.slowqueries";
    public static final String CLUSTERING_ENABLED = "clustering.enabled";
    public static final String CACHING_LITERAL_SIZE = "caching.literal.size";
    public static final String CACHING_BNODE_SIZE = "caching.bnode.size";
    public static final String CACHING_URI_SIZE = "caching.uri.size";
    public static final String CACHING_TRIPLE_SIZE = "caching.triple.size";
    public static final String CLUSTERING_NAME = "clustering.name";
    public static final String CACHING_QUERY_ENABLED = "caching.query.enabled";
    public static final String CONTEXTS_DEFAULT = "contexts.default";
    public static final String CONTEXTS_INFERRED = "contexts.inferred";
    public static final String CLUSTERING_PORT = "clustering.port";
    public static final String CLUSTERING_ADDRESS = "clustering.address";

    public static final String DATABASE_URL = "database.url";
    public static final String DATABASE_USER = "database.user";
    public static final String DATABASE_PASSWORD = "database.password";

    public static final String TRIPLES_BATCHCOMMIT = "database.triples.batchcommit";
    public static final String TRIPLES_BATCHSIZE = "database.triples.batchsize";
    public static final String CLUSTERING_BACKEND = "clustering.backend";
    public static final String CLUSTERING_MODE = "clustering.mode";
}
