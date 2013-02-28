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
package org.apache.marmotta.platform.core.api.modules;

import java.net.URL;

/**
 * A service for resolving and accessing resources contained in the LMF modules. The resource service takes care
 * of retrieving, caching and refreshing resources from the appropriate locations.
 * <p/>
 * Note that the resource service is not to be confused with the RDF resources maintained by the server. It is
 * purely meant to retrieve static non-Java resources contained in the modules and web application.
 *
 * User: sschaffe
 */
public interface MarmottaResourceService {


    /**
     * Return the resource identified by the relative URL passed as argument. The passed argument is relative
     * to the web application root of this web application.
     *
     * @param relativeURL a URL relative to the web application root of this web application
     * @return the resource identified by the relative URL, or null if it does not exist
     */
    public ResourceEntry getResource(String relativeURL);


    /**
     * Return the file system URL of the resource identified by the relative HTTP URL passed as argument.
     * he passed argument is relative to the web application root of this web application.
     *
     * @param relativeURL a URL relative to the web application root of this web application
     * @return the file system URL of the resource, regardless whether it actually exists or not
     */
    public URL resolveResource(String relativeURL);
}
