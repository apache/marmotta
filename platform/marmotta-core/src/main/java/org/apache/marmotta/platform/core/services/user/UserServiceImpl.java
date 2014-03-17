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
package org.apache.marmotta.platform.core.services.user;

import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.events.SystemRestartedEvent;
import org.apache.marmotta.platform.core.events.SystemRestartingEvent;
import org.apache.marmotta.platform.core.exception.UserExistsException;
import org.apache.marmotta.platform.core.model.user.MarmottaUser;
import org.apache.marmotta.platform.core.qualifiers.user.AdminUser;
import org.apache.marmotta.platform.core.qualifiers.user.AnonymousUser;
import org.apache.marmotta.platform.core.qualifiers.user.CurrentUser;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.marmotta.commons.sesame.model.Namespaces.ADMIN_LOGIN;
import static org.apache.marmotta.commons.sesame.model.Namespaces.ANONYMOUS_LOGIN;
import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
@Named("kiwi.core.userService")
@ApplicationScoped
public class UserServiceImpl implements UserService {

    @Inject
    private Logger log;

    @Inject
    private SesameService sesameService;

    @Inject
    private ConfigurationService configurationService;

    /**
     * Each thread gets its own User. By using {@link InheritableThreadLocal}, the user is inherited
     * by from the parent thread unless it is explicitly set.
     */
    private static InheritableThreadLocal<URI> currentUser = new InheritableThreadLocal<URI>();

    // marker to ensure that no other thread interferes while setting up default users ...
    private boolean users_created = false;

    private final Lock lock = new ReentrantLock();


    private URI adminUser;
    private URI anonUser;


    /**
     * initialize() initializes the anonymous and admin user
     * if the database has not yet been set up.
     */
    @PostConstruct
    public void initialize() {
        log.info("initialising user service ...");
    }


    /**
     * Create the admin and anonymous user in the database. Must be called before any other method can be used.
     */
    @Override
    public  void createDefaultUsers() {
        lock.lock();
        try  {
            if (!userExists(ANONYMOUS_LOGIN)) {
                log.debug("Initializing anonymous user.");
                try {
                    anonUser = createUser(ANONYMOUS_LOGIN);
                } catch(UserExistsException e) {
                    log.debug("Anonymous user already exists.");
                }
            } else {
                anonUser = getUserByLogin(ANONYMOUS_LOGIN);
            }

            if (!userExists(ADMIN_LOGIN)) {
                log.debug("Initializing admin.");
                try {
                    adminUser = createUser(ADMIN_LOGIN);
                } catch(UserExistsException e) {
                    log.debug("Admin already exists.");
                }
            } else {
                adminUser = getUserByLogin(ADMIN_LOGIN);
            }

            users_created = true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * When system is restarted, flush all cache data
     * @param e
     */
    public void systemRestart(@Observes SystemRestartingEvent e) {
        log.warn("system restarted, clearing user caches");

        clearCurrentUser();
        adminUser = null;
        anonUser  = null;
        users_created = false;
    }

    /**
     * When database is re-initialised, create default users
     * @param e
     */
    public void systemRestarted(@Observes SystemRestartedEvent e) {
        log.warn("system finished restarting, recreating default users");
        createDefaultUsers();
    }

    /**
     * Return the currently active user. The method tries to determine the current user using the following means:
     * - the user stored in the session, if existent
     * - the user authenticated using HTTP authentication, if existent
     * - the anonymous user
     *
     * @return
     */
    @Override
    @Produces @CurrentUser
    public URI getCurrentUser() {
        if(currentUser.get() == null)
            return getAnonymousUser();
        else
            return currentUser.get();
    }



    /**
     * Set the current user to the user  passed as argument. The current user should be associated with
     * the current session in a thread local variable that is cleared again when the request finishes
     * (KiWiUserTokenFilter)
     *
     * @param user - the resource that represents the user
     */
    @Override
    public void setCurrentUser(URI user) {
        currentUser.set(user);
    }

    /**
     * Clear a current user setting for the current thread. Clears the thread local variable set for the
     * currently running thread.
     */
    @Override
    public void clearCurrentUser() {
        currentUser.remove();
    }



    /**
     * Return the anonymous user. If it does not exist yet, it is created in the database and stored.
     *
     * @return
     */
    @Override
    @Produces @AnonymousUser
    public URI getAnonymousUser() {
        while(anonUser == null && !users_created) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            log.warn("anonymous user not yet created; waiting for creation to finish");
        }

        return anonUser;
    }

    @Override
    public boolean isAnonymous(URI user) {
        return getAnonymousUser().equals(user);
    }

    @Override
    @Produces @AdminUser
    public URI getAdminUser() {
        while(adminUser == null && !users_created) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            log.warn("admin user not yet created; waiting for creation to finish");
        }

        return adminUser;
    }


    /**
     * Create a new user with the provided login. The method first
     * checks of a user with this login already exists; if yes, an exception is thrown. The
     * method does not persist the user; this needs to be done by the caller.
     *
     *
     *
     * @param login login of the user to create
     * @return the newly created user.
     */
    @Override
    public URI createUser(String login) throws UserExistsException {
        return createUser(login, null, null);
    }



    /**
     * Create a new user with the provided login, first name and last name. The method first
     * checks of a user with this login already exists; if yes, an exception is thrown. The
     * method does not persist the user; this needs to be done by the caller.
     *
     *
     *
     * @param login login of the user to create
     * @param firstName first name of the user to create
     * @param lastName last name of the user to create
     * @return the newly created user.
     */
    @Override
    public URI createUser(final String login, final String firstName, final String lastName) throws UserExistsException {
        lock.lock();
        try {
            if(!userExists(login)) {
                String webId_str = buildUserUri(login);

                log.info("creating user with webId: {} ", webId_str);


                String template = "user.%s.%s";

                configurationService.setConfiguration(String.format(template, login, "webid"), webId_str);
                configurationService.setConfiguration(String.format(template, login, "first"), firstName);
                configurationService.setConfiguration(String.format(template, login, "last"),  lastName);


                try {
                    RepositoryConnection conn = sesameService.getConnection();
                    try {
                        conn.begin();
                        URI webId = conn.getValueFactory().createURI(webId_str);

                        if (!login.equals(ANONYMOUS_LOGIN) && !login.equals(ADMIN_LOGIN)) {
                            MarmottaUser u = FacadingFactory.createFacading(conn).createFacade(webId, MarmottaUser.class);
                            u.setFirstName(firstName);
                            u.setLastName(lastName);
                            u.setNick(login);
                        }

                        if(login.equals(ANONYMOUS_LOGIN)) {
                            anonUser = webId;
                        }
                        if(login.equals(ADMIN_LOGIN)) {
                            adminUser = webId;
                        }

                        conn.commit();
                        
                        return webId;
                    } finally {
                        conn.close();
                    }
                } catch(RepositoryException ex) {
                    handleRepositoryException(ex);
                    return null;
                }

            } else
                throw new UserExistsException("User "+login+" already exists, cannot create!");
        } finally {
            lock.unlock();
        }
    }

    private String buildUserUri(final String login) {
        return configurationService.getBaseUri() + "user/" + login;
    }


    /**
     * Return a user by login. The user is looked up in the database and returned. In case
     * no user with this login is found, this method returns null.
     *
     *
     *
     * @param login the login to look for
     * @return the user with the given login, or null if no such user exists
     */
    @Override
    public URI getUser(String login) {
        return getUserByLogin(login);
    }

    /**
     * Return a user by login. The user is looked up in the database and returned. In case
     * no user with this login is found, this method returns null. The second parameter
     * specifies whether a failure should be logged or accepted silently. A silent progress
     * may be useful in case we just want to check whether a user exists.
     *
     *
     * @param login the login to look for
     * @return the user with the given login, or null if no such user exists
     */
    private URI getUserByLogin(String login) {
        String webId = configurationService.getStringConfiguration(String.format("user.%s.webid", login));

        if(webId != null) {
            return sesameService.getRepository().getValueFactory().createURI(webId);
        } else {
            return null;
        }
    }

    /**
     * Check whether a user with the given login already exists. If so,
     * returns true. Otherwise returns false.
     *
     * @param login the login to look for
     * @return true if a user with this login already exists, false otherwise
     * @see org.apache.marmotta.platform.core.api.user.UserService#userExists(java.lang.String)
     */
    @Override
    public boolean userExists(String login) {
        return getUserByLogin(login) != null;
    }

}
