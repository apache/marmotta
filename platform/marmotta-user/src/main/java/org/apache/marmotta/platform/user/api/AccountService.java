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

import org.apache.marmotta.platform.core.model.user.MarmottaUser;
import org.apache.marmotta.platform.user.model.UserAccount;
import org.openrdf.model.URI;

import java.util.List;
import java.util.Set;

/**
 * Service to manage UserAccounts. {@link UserAccount}s are used to manage login and access to the
 * LMF System.
 * 
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 * @see UserAccount
 */
public interface AccountService {

    /**
     * Create an user account
     * 
     * @param login the unique username/login
     * @return the newly created {@link UserAccount}
     */
    UserAccount createAccount(String login);

    /**
     * Create an user account and set first- and lastName in the user profile (foaf)
     * 
     * @param login the unique username/login (also foaf:nick)
     * @param firstName the value for foaf:firstName, may be null
     * @param lastName the value for foaf:lastName, may be null
     * @return the newly created {@link UserAccount}
     */
    UserAccount createAccount(String login, String firstName, String lastName);

    /**
     * Create the default accounts, currently only admin
     */
    void createDefaultAccounts();

    /**
     * Retrieve the user account for the given login name
     * 
     * @param login the login name
     * @return the {@link UserAccount} for the given login name
     */
    UserAccount getAccount(String login);

    /**
     * Retrieve the user account for the given (User)-Resource
     * 
     *
     * @param userResource the user resource (foaf:person)
     * @return the corresponding {@link UserAccount}, or <code>null</code> if no account present.
     */
    UserAccount getAccount(URI userResource);

    /**
     * Retrieve the user account for a given {@link org.apache.marmotta.platform.core.model.user.MarmottaUser} (facaded user resource)
     * 
     * @param user the {@link org.apache.marmotta.platform.core.model.user.MarmottaUser}
     * @return the corresponding {@link UserAccount}, of <code>null</code> if none present.
     * @see #getAccount(org.openrdf.model.URI)
     */
    UserAccount getAccount(MarmottaUser user);

    /**
     * Deletes the given user account. The user will (obviously loose all access-rights to the
     * system)
     * 
     * @param account the {@link UserAccount} to delete.
     */
    void deleteAccount(UserAccount account);

    /**
     * Update/Set the password for the given {@link UserAccount}
     * 
     * @param account the {@link UserAccount} to modify
     * @param passwd the new password
     * @return the modified {@link UserAccount}
     */
    UserAccount setPassword(UserAccount account, String passwd);

    /**
     * Check the credentials for the given {@link UserAccount}
     * 
     * @param account the {@link UserAccount} to authenticate
     * @param passwd the password (plain)
     * @return <code>true</code> if the password matched the password of the {@link UserAccount}
     */
    boolean checkPassword(UserAccount account, String passwd);

    /**
     * Check the credentials for the given login
     * 
     * @param login the login/account name/user name
     * @param passwd the password (plain)
     * @return <code>true</code> if the password matched the logins' {@link UserAccount} password
     */
    boolean checkPassword(String login, String passwd);

    /**
     * Set the roles for the given {@link UserAccount}
     * 
     * @param account the {@link UserAccount} to modify
     * @param roles the roles (names) of the account
     * @see UserAccount#setRoles(Set)
     */
    void setRoles(UserAccount account, Set<String> roles);

    /**
     * Retrieve the roles for the given {@link UserAccount}
     * 
     * @param account the {@link UserAccount}
     * @return a {@link Set} containing the role-names of the given {@link UserAccount}
     * @see UserAccount#getRoles()
     */
    Set<String> getRoles(UserAccount account);

    /**
     * Add a single role to the roles of the given {@link UserAccount}
     * 
     * @param account the {@link UserAccount} to modify
     * @param role the role(-name) to add
     */
    void addRole(UserAccount account, String role);

    /**
     * Remove a single role from the roles of the given {@link UserAccount}
     * 
     * @param account the {@link UserAccount} to modify
     * @param role the role(-name) to remove
     */
    void removeRole(UserAccount account, String role);

    /**
     * Check whether the given {@link UserAccount} has the role in question.
     * 
     * @param account the {@link UserAccount} to query
     * @param role the role(-name) in question.
     * @return true if the given {@link UserAccount} has the role in question
     */
    boolean hasRole(UserAccount account, String role);

    /**
     * Returns a {@link List} of {@link UserAccount} that have the given role associated.
     * 
     * @param role the role(-name)
     * @return a {@link List} of {@link UserAccount} that have the given role associated.
     */
    List<UserAccount> listAccounts(String role);

    /**
     * List all {@link UserAccount}.
     * 
     * @return a {@link List} of all {@link UserAccount}
     */
    List<UserAccount> listAccounts();



}
