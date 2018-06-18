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
package org.apache.marmotta.platform.user.services.auth;

import org.apache.marmotta.platform.user.api.AuthenticationProvider;
import org.apache.marmotta.platform.user.model.UserAccount;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Authenticate LMF-Users against LDAP.
 * 
 * TODO: maybe switch to jldap (compile 'com.novell.ldap:jldap:4.3') which might also allow password
 * change.
 * TODO: password-update is currently not implemented.
 * 
 * <h3>Configuration Settings</h3>
 * <dl>
 * <dt>user.auth.ldap.server
 * <dd>hostname/IP of the ldap-server (default: <b>localhost</b>)
 * <dt>user.auth.ldap.port
 * <dd>ldap server port (default: <b>389</b>)
 * <dt>user.auth.ldap.dn
 * <dd>Pattern to build the DN for auth. <code>{login}</code> will be replaced by the account
 * name/login (default: <b>{login}</b>)
 * </dl>
 * 
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 * @author Daniel Trabe <daniel.trabe@salzburgresearch.at>
 * 
 */
@ApplicationScoped
@Named(LdapAuthProvider.QUALIFIER)
public class LdapAuthProvider implements AuthenticationProvider {

    static final String          QUALIFIER   = "ldap";
    static final String          CONF_SERVER = "user.auth." + QUALIFIER + ".server";
    static final String          CONF_PORT   = "user.auth." + QUALIFIER + ".port";
    static final String          CONF_DN     = "user.auth." + QUALIFIER + ".dn";


    @Inject
    private ConfigurationService configurationService;

    @Inject
    private Logger               log;

    @Override
    public boolean checkPassword(UserAccount login, String passwd) {
        return login != null && login(login.getLogin(), passwd);
    }

    @Override
    public boolean updatePassword(UserAccount login, String newPasswd) {
        if (login == null) return false;
        String username = login.getLogin();
        log.trace("changePassword called for account: {}", username);

        ModificationItem[] mod = new ModificationItem[1];
        Attribute attr = new BasicAttribute("userpassword", newPasswd);
        mod[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);

        final String dn = configurationService.getStringConfiguration(CONF_DN, "{login}")
                .replaceAll(Pattern.quote("{login}"), username);
        try {
            // ctx.modifyAttributes(dn, mod);
            // log.info("LDAP-Passwd update for {} successful ({})", username, dn);
            // return true;
        } catch (Exception e) {
            log.info("LDAP-Passwd update for {} failed ({})", username, dn);
        }
        log.warn("LDAP-Passwd update not implemented");
        return false;
    }

    private boolean login(String login, String secret) {
        try {
            // Set up the environment for creating the initial context
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, String.format("ldap://%s:%d",
                    configurationService.getStringConfiguration(CONF_SERVER, configurationService.getServerName()),
                    configurationService.getIntConfiguration(CONF_PORT, 389)));

            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, configurationService.getStringConfiguration(CONF_DN, "{login}")
                    .replaceAll(Pattern.quote("{login}"), login));
            env.put(Context.SECURITY_CREDENTIALS, secret);

            // Create the initial context
            DirContext ctx = new InitialDirContext(env);
            // If retrieving the context worked, login was successful.
            boolean result = ctx != null;

            if (ctx != null) {
                ctx.close();
            }

            log.trace("LDAP-Login successful for {}", login);
            return result;
        } catch (Exception e) {
            log.info("LDAP-Login for {} failed: {}", login, e.getMessage());
            return false;
        }
    }

}
