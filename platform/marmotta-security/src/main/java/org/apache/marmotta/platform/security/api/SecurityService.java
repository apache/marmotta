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
package org.apache.marmotta.platform.security.api;

import org.apache.marmotta.platform.security.model.SecurityConstraint;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * A service that provides access control and other security functionality.
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface SecurityService {

    /**
     * Check whether access is granted for the given request. Returns true if the security system grants access.
     * Returns false if access is denied; in this case, the caller may decide to sent an authorization request to
     * the client.
     *
     * @param request
     * @return true in case the active security constraints grant access, false otherwise
     */
    public boolean grantAccess(HttpServletRequest request);

    /**
     * Load a pre-configured security profile from the classpath. When calling this method, the service will
     * look for files called security-profile.<name>.properties and replace all existing security constraints by
     * the new security constraints.
     * @param profile
     */
    public void loadSecurityProfile(String profile);

    /**
     * List all security constraints, ordered by priority.
     * @return
     */
    public List<SecurityConstraint> listSecurityConstraints();

    /**
     * Does nothing, just ensures the security service is properly initialised.
     * TODO: this is a workaround and should be fixed differently.
     */
    public void ping();

}
