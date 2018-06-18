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
package org.apache.marmotta.platform.user.api;

import java.util.Set;

/**
 * A service that abstracts simple user authentication. The standard backend implements authentication from the
 * LMF configuration file using the following configuration properties:
 * security.user.<username>.password  - the plaintext password of the user with login <username>
 * security.user.<username>.roles     - the roles of the user with login <username>
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface AuthenticationService {

    /**
     * Authenticate the user with the given login and password. Returns true on success, false if the user does not
     * exist or the passwords do not match.
     *
     * @param login  login of the user to authenticate
     * @param password password of the user to authenticate
     * @return true on success, false if the user does not exist or the passwords do not match.
     */
    boolean authenticateUser(String login, String password);


    /**
     * Change the password of the user with the given login to the given new password. The implementation may decide
     * where to persist the password in a secure manner and whether to apply additional security like password hashing.
     *
     * @param login
     * @param password
     * @return
     */
    void setUserPassword(String login, String password);


    /**
     * Return the roles that are assigned to the user (a list of strings that can be chosen by the administrator as
     * needed).
     * @param login login name of the user for whom to return the roles
     * @return a list of strings with the role names currently assigned to the user
     */
    Set<String> listUserRoles(String login);


    /**
     * Add the role with the given name to the user with the given login.
     *
     * @param login the login name of the user with whom to associate roles
     * @param role  the role name to associate with the user
     */
    void addUserRole(String login, String role);

    /**
     * Remove the role with the given name from the user with the given login.
     *
     * @param login the login name of the user from whom to remove the role
     * @param role  the role name to remove from the list of roles of the user
     */
    void removeUserRole(String login, String role);

    /**
     * Returns a list of available {@link AuthenticationProvider} names.
     */
    Set<String> listAuthProviderNames();
}
