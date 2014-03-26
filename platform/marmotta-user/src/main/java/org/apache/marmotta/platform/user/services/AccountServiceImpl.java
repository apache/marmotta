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
package org.apache.marmotta.platform.user.services;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.core.exception.UserExistsException;
import org.apache.marmotta.platform.core.model.user.MarmottaUser;
import org.apache.marmotta.platform.core.qualifiers.cache.MarmottaCache;
import org.apache.marmotta.platform.user.api.AccountService;
import org.apache.marmotta.platform.user.model.UserAccount;
import org.apache.marmotta.platform.user.model.UserAccount.PasswordHash;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class AccountServiceImpl implements AccountService {

    @Inject
    private Logger               log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private UserService          userService;

    @Inject
    @MarmottaCache("user-cache")
    private ConcurrentMap userCache;

    private PasswordHash         hashAlgo;


    public AccountServiceImpl() {
        hashAlgo = PasswordHash.SHA1;
    }

    @PostConstruct
    public void initialize() {
        final String conf = configurationService.getStringConfiguration("security.password.hash", "SHA1").toUpperCase();
        try {
            hashAlgo = PasswordHash.valueOf(conf);
        } catch (Exception e) {
            hashAlgo = PasswordHash.SHA1;
            log.warn("Invalid/unknown password hash algorithm: {}, falling back to {}", conf, hashAlgo);
        }
    }

    public void systemStartup(@Observes SystemStartupEvent event) {
        log.info("creating default system accounts ...");
        createDefaultAccounts();
    }

    public void onConfigurationChange(@Observes ConfigurationChangedEvent event) {
        if (event.containsChangedKey("security.password.hash")) {
            initialize();
        }
    }

    @Override
    public void createDefaultAccounts() {
        // Currently there is only one default account: admin
        UserAccount account = getAccount(Namespaces.ADMIN_LOGIN);
        if (account == null) {
            Set<String> roles = new HashSet<String>(configurationService.getListConfiguration("user." + Namespaces.ADMIN_LOGIN + ".roles"));
            account = createAccount(Namespaces.ADMIN_LOGIN);
            account.setRoles(roles);
            account.setPasswd(hashAlgo, configurationService.getStringConfiguration("user." + Namespaces.ADMIN_LOGIN + ".password"));
            save(account);
        }
    }

    @Override
    public List<UserAccount> listAccounts() {
        Set<String> logins = new HashSet<String>();
        for(String key : configurationService.listConfigurationKeys("user")) {
            String[] components = key.split("\\.");
            if(components.length > 2 && "webid".equals(components[2])) {
                logins.add(components[1]);
            }
        }

        final List<UserAccount> list = new ArrayList<UserAccount>();
        for(String login : logins) {
            list.add(getAccount(login));
        }


        for (UserAccount userAccount : list) {
            userCache.put(userAccount.getLogin(), userAccount);
            userCache.put(userAccount.getWebId(), userAccount);
        }
        return list;
    }

    @Override
    public List<UserAccount> listAccounts(String role) {
        List<UserAccount> result = new ArrayList<UserAccount>();

        for(UserAccount account : listAccounts()) {
            if(account.getRoles().contains(role)) {
                result.add(account);
            }
        }

        return result;
    }

    @Override
    public UserAccount createAccount(String login) {
        return createAccount(login, null, null);
    }

    @Override
    public UserAccount createAccount(String login, String firstName, String lastName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(login), "blank/empty login not allowed");

        URI webid = userService.getUser(login);
        if (webid == null) {
            try {
                webid = userService.createUser(login, firstName, lastName);
            } catch (UserExistsException e) {
                log.warn("User {} exists. This should not happen as it was checked 3 lines before!", login);
                webid = userService.getUser(login);
            }
        }

        UserAccount account = new UserAccount(login, webid.stringValue());

        save(account);

        return account;
    }

    private void save(UserAccount account) {
        configurationService.setConfiguration("user."+account.getLogin()+".pwhash", account.getPasswdHash());
        configurationService.setConfiguration("user."+account.getLogin()+".webid", account.getWebId());
        configurationService.setListConfiguration("user." + account.getLogin() + ".roles", new ArrayList<String>(account.getRoles()));
    }

    @Override
    public void deleteAccount(UserAccount account) {
        for(String key : configurationService.listConfigurationKeys("user."+account.getLogin())) {
            configurationService.removeConfiguration(key);
        }
        userCache.remove(account.getLogin());
        userCache.remove(account.getWebId());
    }

    @Override
    public UserAccount getAccount(String login) {
        if (StringUtils.isBlank(login)) return null;
        UserAccount account = null;
        if (userCache != null && userCache.get(login) != null) {
            account = (UserAccount) userCache.get(login);
        } else {
            if (configurationService.isConfigurationSet("user."+login+".webid")) {
                account = new UserAccount();

                account.setLogin(login);
                account.setPasswdHash(configurationService.getStringConfiguration("user."+login+".pwhash"));
                account.setRoles(new HashSet<String>(configurationService.getListConfiguration("user."+login+".roles")));
                account.setWebId(configurationService.getStringConfiguration("user."+login+".webid"));

                userCache.put(account.getLogin(), account);
                userCache.put(account.getWebId(), account);
            } else {
                log.info("UserAccount {} not found", login);
            }
        }
        return account;
    }

    @Override
    public UserAccount getAccount(URI resource) {
        Preconditions.checkArgument(resource != null);

        UserAccount account = null;
        if (userCache != null && userCache.get(resource) != null) {
            account = (UserAccount) userCache.get(resource);
        } else {
            for(UserAccount a : listAccounts()) {
                if(a.getWebId().equals(resource.stringValue())) {
                    account = a;
                    break;
                }
            }
            if (account != null) {
                userCache.put(account.getLogin(), account);
                userCache.put(account.getWebId(), account);
            } else {
                log.warn("UserAccount {} not found", resource);
            }
        }
        return account;
    }

    @Override
    public UserAccount getAccount(MarmottaUser user) {
        Resource delegate = user.getDelegate();
        if (delegate instanceof URI)
            return getAccount((URI) delegate);
        return null;
    }

    @Override
    public UserAccount setPassword(UserAccount account, String passwd) {
        account.setPasswd(hashAlgo, passwd);
        save(account);
        return account;
    }

    @Override
    public boolean checkPassword(UserAccount account, String passwd) {
        return account != null && account.checkPasswd(passwd);
    }

    @Override
    public boolean checkPassword(String login, String passwd) {
        return getAccount(login) != null && getAccount(login).checkPasswd(passwd);
    }

    @Override
    public void setRoles(UserAccount account, Set<String> roles) {
        account.setRoles(new HashSet<String>(roles));
        save(account);
    }

    @Override
    public Set<String> getRoles(UserAccount account) {
        return account.getRoles();
    }

    @Override
    public void addRole(UserAccount account, String role) {
        account.addRole(role);
        save(account);
    }

    @Override
    public void removeRole(UserAccount account, String role) {
        Set<String> roles = account.getRoles();
        roles.remove(role);
        account.setRoles(roles);
        save(account);
    }

    @Override
    public boolean hasRole(UserAccount account, String role) {
        return account.getRoles().contains(role);
    }


}
