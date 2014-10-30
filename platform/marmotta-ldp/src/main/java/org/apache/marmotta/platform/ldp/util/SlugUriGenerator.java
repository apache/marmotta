/*
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
package org.apache.marmotta.platform.ldp.util;

import org.apache.marmotta.platform.ldp.api.LdpService;
import org.openrdf.repository.RepositoryConnection;

/**
 * Slug-Header based Generator for resource URIs.
 */
public class SlugUriGenerator extends AbstractResourceUriGenerator {

    private final String slug;
    private int i = 0;

    public SlugUriGenerator(LdpService ldpService, String container, String slug, RepositoryConnection connection) {
        super(ldpService, container, connection);

        String localName = LdpUtils.urify(slug);
        log.trace("Slug urified: {}", localName);

        this.slug = localName;
    }

    @Override
    protected String generateNextLocalName() {
        if (i < 1) {
            i++;
            return slug;
        } else {
            return String.format("%s-%d", slug, i++);
        }
    }

}
