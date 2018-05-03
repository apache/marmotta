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
package org.apache.marmotta.platform.user.services;

import org.apache.marmotta.platform.core.util.CDIUtils;
import org.apache.marmotta.platform.user.api.AccountService;
import org.apache.marmotta.platform.user.api.AuthenticationProvider;
import org.apache.marmotta.platform.user.api.AuthenticationService;
import org.apache.marmotta.platform.user.model.UserAccount;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple implementation of an authentication that stores plain-text passwords in the system configuration.
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class AuthenticationServiceImpl implements AuthenticationService {

    public static final String DEFAULT_AUTH_PROVIDER_NAMED = "lmf";

    @Inject
    private Logger log;

    @Inject
    private AccountService accountService;

    @Inject
    private ConfigurationService configurationService;

    @Inject @Any
    private Instance<AuthenticationProvider> providers;

    private AuthenticationProvider authenticationProvider;

    @PostConstruct
    protected void initialize() {
        log.debug("initializing AuthenticationService");

        final String prov = configurationService.getStringConfiguration("user.auth.provider", DEFAULT_AUTH_PROVIDER_NAMED);

        Instance<AuthenticationProvider> selected = CDIUtils.selectNamed(providers, prov);
        if (selected.isAmbiguous()) {
            authenticationProvider = selected.iterator().next();
            log.error("multiple candidates for AuthenticationProvider '{}' found. Chose randomly!", prov);
        } else if (selected.isUnsatisfied()) {
            log.error("no candidate for AuthenticationProvider '{}' found, falling back to default", prov);
            authenticationProvider = CDIUtils.selectNamed(providers, DEFAULT_AUTH_PROVIDER_NAMED).iterator().next();
        } else {
            authenticationProvider = selected.get();
        }
    }

    protected void onConfigurationChange(@Observes ConfigurationChangedEvent event) {
        if (event.containsChangedKey("user.auth.provider")) {
            initialize();
        }
    }

    @Override
    public Set<String> listAuthProviderNames() {
        HashSet<String> pNames = new HashSet<String>();
        for (AuthenticationProvider p : providers) {
            Named ann = p.getClass().getAnnotation(Named.class);
            if (ann != null) {
                pNames.add(ann.value());
            }
        }
        return pNames;
    }


    /**
     * Authenticate the user with the given login and password. Returns true on success, false if the user does not
     * exist or the passwords do not match.
     *
     * @param login    login of the user to authenticate
     * @param password password of the user to authenticate
     * @return true on success, false if the user does not exist or the passwords do not match.
     */
    @Override
    public boolean authenticateUser(String login, String password) {
        log.debug("AUTH {} with {}", login, authenticationProvider != null ? authenticationProvider.getClass().getSimpleName() : null);
        return authenticationProvider.checkPassword(accountService.getAccount(login), password);
    }


    /**
     * Change the password of the user with the given login to the given new password. The implementation may decide
     * where to persist the password in a secure manner and whether to apply additional security like password hashing.
     *
     * @param login
     * @param password
     * @return
     */
    @Override
    public void setUserPassword(String login, String password) {
        final UserAccount account = accountService.getAccount(login);
        authenticationProvider.updatePassword(account, password);
    }

    /**
     * Return the roles that are assigned to the user (a list of strings that can be chosen by the administrator as
     * needed).
     *
     * @param login login name of the user for whom to return the roles
     * @return a list of strings with the role names currently assigned to the user
     */
    @Override
    public Set<String> listUserRoles(String login) {
        return accountService.getRoles(accountService.getAccount(login));
    }


    /**
     * Add the role with the given name to the user with the given login.
     *
     * @param login the login name of the user with whom to associate roles
     * @param role  the role name to associate with the user
     */
    @Override
    public void addUserRole(String login, String role) {
        final UserAccount a = accountService.getAccount(login);
        accountService.addRole(a, role);
    }

    /**
     * Remove the role with the given name from the user with the given login.
     *
     * @param login the login name of the user from whom to remove the role
     * @param role  the role name to remove from the list of roles of the user
     */
    @Override
    public void removeUserRole(String login, String role) {
        final UserAccount a = accountService.getAccount(login);
        accountService.removeRole(a, role);
    }


}
