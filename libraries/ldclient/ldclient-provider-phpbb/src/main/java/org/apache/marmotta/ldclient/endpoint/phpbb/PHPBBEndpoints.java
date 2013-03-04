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

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for simplifying registering of PHPBB endpoints
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class PHPBBEndpoints {

    /**
     * Construct a set of PHPBB endpoints from a PHPBB base URL and a name
     *
     * @param phpBBUrl base URL of the PHPBB installation, e.g. "http://www.carving-ski.de/phpBB/"
     * @param name a name for the endpoints (e.g. "carving-ski.de")
     * @return a forum endpoint, a post endpoint and a topic endpoint
     */
    public static Set<Endpoint> getEndpoints(String phpBBUrl, String name) {
        Set<Endpoint> result = new HashSet<Endpoint>();
        String pattern = "^" + Pattern.quote(phpBBUrl);
        result.add(new PHPBBForumEndpoint("PHPBB Forum ("+name+")", pattern + "viewforum.php\\?.*f=.*"));
        result.add(new PHPBBTopicEndpoint("PHPBB Topics ("+name+")", pattern + "viewtopic.php\\?.*t=.*"));
        result.add(new PHPBBPostEndpoint("PHPBB Posts ("+name+")", pattern + "viewtopic.php\\?.*p=.*"));
        return result;
    }
}
