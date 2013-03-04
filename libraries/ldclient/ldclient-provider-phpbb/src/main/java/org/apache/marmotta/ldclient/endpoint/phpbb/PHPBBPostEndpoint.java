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
package org.apache.marmotta.ldclient.endpoint.phpbb;

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.provider.phpbb.PHPBBPostProvider;

/**
 * Create a new PHPBBForumEndpoint; the URL pattern for the forum needs to be passed as argument, e.g.
 * "^http://www\\.carving-ski\\.de/phpBB/viewtopic.php\\?.*p=.*"
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class PHPBBPostEndpoint extends Endpoint {

    public PHPBBPostEndpoint(String name, String urlPattern) {
        super(name, PHPBBPostProvider.PROVIDER_NAME, urlPattern, null, 86400L);
        setPriority(PRIORITY_HIGH);
        addContentType(new ContentType("text", "html"));
    }
}
