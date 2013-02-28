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
package org.apache.marmotta.platform.core.api.user;

import org.apache.marmotta.platform.core.exception.UserExistsException;
import org.openrdf.model.URI;

/**
 * Manages the user-resources (BASE_URL/users/*)
 * 
 * @author sschaffe
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 */
public interface UserService {

    /* Current User Management */

    /**
     * Return the currently active user. The method tries to determine the current user using the
     * following means:
     * <ol>
     * <li>the user stored in the session, if existant
     * <li>the user authenticated using HTTP authentication, if existant
     * <li>the anonymous user
     * </ol>
     * 
     * Child-Threads will inherit the current user from it's parent thread unless the current user
     * was explicitly set.
     * 
     * @see #setCurrentUser(org.openrdf.model.URI)
     * 
     * @return the resource for the current user.
     */
    public URI getCurrentUser();

    /**
     * Check whether the given resource is the anonyous user resource
     * 
     *
     * @param user the resource to check
     * @return <code>true</code> if the given resource equals the anonymous user resource.
     * @see #getAnonymousUser()
     */
    public boolean isAnonymous(URI user);

    /**
     * Set the current user to the user passed as argument. The current user should be associated
     * with the current session in a thread local variable that is cleared again when the request
     * finishes (KiWiUserTokenFilter)
     *
     * @param user
     */
    public void setCurrentUser(URI user);

    /**
     * Clear a current user setting for the current thread. Clears the thread local variable set for
     * the currently running thread.
     * 
     * This will revert the the current user to the state as it had never been set, i.e. inheriting
     * from the parent thread.
     */
    public void clearCurrentUser();

    /* User Token Management */

    /**
     * Create a new user with the provided login. The method first checks of a user with this login
     * already exists; if yes, an exception is thrown.
     * 
     *
     * @param login login of the user to create
     * @return the newly created user.
     */
    public URI createUser(String login) throws UserExistsException;

    /**
     * Create a new user with the provided login, first name and last name. The method first
     * checks of a user with this login already exists; if yes, an exception is thrown.
     * 
     *
     * @param login login of the user to create
     * @param firstName first name of the user to create
     * @param lastName last name of the user to create
     * @return the newly created user.
     */
    public URI createUser(final String login, final String firstName, final String lastName) throws UserExistsException;

    /**
     * Return the anonymous user. If it does not exist yet, it is created in the database and
     * stored.
     * 
     * @return the {@link org.apache.marmotta.kiwi.model.rdf.KiWiUriResource} representing the anonymous user.
     */
    public URI getAnonymousUser();

    /**
     * Return the (default) admin user. If it does not exist yet, it is created in the database and
     * stored.
     * 
     * @return the {@link org.apache.marmotta.kiwi.model.rdf.KiWiUriResource} representing the user "admin".
     */
    public URI getAdminUser();

    /**
     * Return a user by login. The user is looked up in the database and returned. In case
     * no user with this login is found, this method returns null.
     * 
     *
     * @param login the login to look for
     * @return the user with the given login, or null if no such user exists
     */
    public URI getUser(String login);

    /**
     * Check whether the user with the given login name already exists.
     * 
     * @param login the username to check
     * @return true if it exists.
     */
    public boolean userExists(String login);

    /**
     * Create the default users (namely "admin" and "anonymous").
     */
    public void createDefaultUsers();

}
