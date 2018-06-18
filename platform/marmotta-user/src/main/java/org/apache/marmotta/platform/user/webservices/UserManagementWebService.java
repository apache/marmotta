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
package org.apache.marmotta.platform.user.webservices;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.user.api.AccountService;
import org.apache.marmotta.platform.user.model.UserAccount;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/users")
public class UserManagementWebService {
    private static final Pattern PROFILE_URI_PATTERN = Pattern.compile("^<([^>]+)>$");

    @Inject
    private Logger         log;

    @Inject
    private AccountService accountService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    private List<String>         acceptedFoafProperties;

    private static final List<String> RESERVED_LOGINS     = Arrays.asList("me", "login", "anonymous");

    @PostConstruct
    public void initialize() {
        acceptedFoafProperties = configurationService.getListConfiguration("user.account.foaf.properties",
                Arrays.asList("firstName", "nick", "lastName", "familyName", "givenName", "name", "title", "age", "mbox", "homepage"));
    }

    /**
     * List users (that have the given role).
     *
     * @param role the required role (optional)
     * @return an array of {@link org.apache.marmotta.platform.user.webservices.UserWebService.AccountPoJo}s containing users (that have the required role, if
     *         role was given) (in JSON).
     */
    @GET
    @Produces(Namespaces.MIME_TYPE_JSON)
    public Response listUsers(@QueryParam("role") String role) {
        final List<UserAccount> accounts;
        if (role == null) {
            accounts = accountService.listAccounts();
        } else {
            accounts = accountService.listAccounts(role);
        }

        final List<UserWebService.AccountPoJo> resp = new ArrayList<UserWebService.AccountPoJo>();
        for (UserAccount userAccount : accounts) {
            UserWebService.AccountPoJo apj = new UserWebService.AccountPoJo(userAccount.getLogin(), userAccount.getWebId());
            apj.setRoles(userAccount.getRoles());
            resp.add(apj);
        }

        return Response.ok(resp, Namespaces.MIME_TYPE_JSON).build();
    }

    /**
     * Create a new user account (incl. user resource)
     *
     * @param login the account name / login name of the new user.
     * @return the {@link org.apache.marmotta.platform.user.webservices.UserWebService.AccountPoJo} of the newly created user.
     * @HTTP 409 if an account with the given login already exists
     * @HTTP 400 if the login is a reserved keyword: [me, login, anonymous]
     * @HTTP 500 on other errors.
     */
    @POST
    @Path("/{login}")
    public Response createUser(@PathParam("login") String login) {
        if (accountService.getAccount(login) != null)
            return Response.status(Status.CONFLICT).entity(String.format("'%s' already exists!", login)).build();

        if (StringUtils.isBlank(login)) return Response.status(Status.BAD_REQUEST).entity("Provide a username").build();

        // Must not create an account with a reserved username!
        if (RESERVED_LOGINS.contains(login))
            return Response.status(Status.BAD_REQUEST).entity(String.format("The following usernames are not allowed: %s", RESERVED_LOGINS)).build();

        UserAccount a = accountService.createAccount(login);
        if (a != null)
            return getUser(login);

        log.error("Creating an account for {} failed", login);
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Sorry, don't know why").build();
    }

    /**
     * Return the Account data of the requested login
     *
     * @param login the account requested
     * @return an {@link org.apache.marmotta.platform.user.webservices.UserWebService.AccountPoJo} of the requested account
     * @HTTP 404 if no such user exists.
     */
    @GET
    @Path("/{login}")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public Response getUser(@PathParam("login") String login) {
        UserAccount account = accountService.getAccount(login);
        if (account == null) return Response.status(Status.NOT_FOUND).entity(String.format("No login for '%s' found!", login)).build();

        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                UserWebService.AccountPoJo apj = new UserWebService.AccountPoJo(account.getLogin(), account.getWebId());
                apj.setRoles(account.getRoles());

                RepositoryResult<Statement> triples = conn.getStatements(conn.getValueFactory().createURI(account.getWebId()),null,null,true);

                while(triples.hasNext()) {
                    Statement t = triples.next();

                    String prop = t.getPredicate().stringValue();
                    if (prop.startsWith(Namespaces.NS_FOAF)) {
                        Value object = t.getObject();
                        if (object instanceof URI) {
                            apj.setFoaf(prop, String.format("<%s>", object));
                        } else if (object instanceof Literal) {
                            apj.setFoaf(prop, object.toString());
                        }
                    }
                }

                return Response.ok(apj, Namespaces.MIME_TYPE_JSON).build();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    /**
     * Delete the account with the given login.
     *
     * @param login the account to delete
     * @param delFoaf if <code>true</code>, also delete the user profile (foaf)
     * @return 200 ok on success
     * @HTTP 404 if no such user exists
     */
    @DELETE
    @Path("/{login}")
    public Response deleteUser(@PathParam("login") String login, @QueryParam("deleteFoaf") @DefaultValue("false") boolean delFoaf) {
        UserAccount account = accountService.getAccount(login);
        if (account == null) return Response.status(Status.NOT_FOUND).entity(String.format("No login for '%s' found!", login)).build();

        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                if (delFoaf && account.getWebId() != null) {
                    // TODO: Remove only users foaf profile?
                    conn.remove(conn.getValueFactory().createURI(account.getWebId()),null,null);
                }

                accountService.deleteAccount(account);
                return Response.status(Status.OK).entity("login removed").build();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    /**
     * Set the roles for the given account
     *
     * @param login the account
     * @param roles <code>role</code> params of the roles to set
     * @param roles2 <code>role[]</code> params of the roles to set
     * @return the {@link org.apache.marmotta.platform.user.webservices.UserWebService.AccountPoJo} of the account after update
     * @HTTP 404 if no such account exists.
     */
    @POST
    @Path("/{login}/roles")
    public Response setUserRoles(@PathParam("login") String login, @QueryParam("role") String[] roles, @QueryParam("role[]") String[] roles2) {
        UserAccount account = accountService.getAccount(login);
        if (account == null) return Response.status(Status.NOT_FOUND).entity(String.format("No login for '%s' found!", login)).build();

        HashSet<String> roleSet = new HashSet<String>();
        for (String role : roles) {
            roleSet.add(role);
        }
        for (String role : roles2) {
            roleSet.add(role);
        }
        accountService.setRoles(account, roleSet);

        return getUser(login);
    }

    /**
     * Set the password for the given account
     *
     * @param login the account to set the password for
     * @param passwd the new password
     * @return 200 OK on success
     * @HTTP 404 if no such account exists
     */
    @POST
    @Path("/{login}/password")
    public Response setUserPassword(@PathParam("login") String login, @FormParam("password") String passwd) {
        UserAccount account = accountService.getAccount(login);
        if (account == null) return Response.status(Status.NOT_FOUND).entity(String.format("No login for '%s' found!", login)).build();

        accountService.setPassword(account, passwd);

        return Response.ok("Password updated").build();
    }

    /**
     * Update/Save the user profile (foaf) for the given account
     *
     * @param login the user to modify
     * @param formParams the user profile (foaf, without prefix) in
     *            {@value Namespaces#MIME_TYPE_FORM_URLENC}
     * @return {@link org.apache.marmotta.platform.user.webservices.UserWebService.AccountPoJo} after the update in JSON
     * @see UserWebService#post(MultivaluedMap)
     * @HTTP 404 if no such user exists.
     */
    @POST
    @Path("/{login}/profile")
    public Response setUserProfile(@PathParam("login") String login, MultivaluedMap<String, String> formParams) {
        UserAccount account = accountService.getAccount(login);
        if (account == null) return Response.status(Status.NOT_FOUND).entity(String.format("No login for '%s' found!", login)).build();

        try {
            RepositoryConnection conn = sesameService.getConnection();

            try {
                String currentUser = account.getWebId();
                for (String prop : formParams.keySet()) {
                    if (!acceptedFoafProperties.contains(prop)) {
                        continue;
                    }

                    String property = Namespaces.NS_FOAF + prop;
                    URI p = conn.getValueFactory().createURI(property);
                    URI u = conn.getValueFactory().createURI(currentUser);
                    ResourceUtils.removeProperty(conn,u, property);
                    String val = formParams.getFirst(prop);
                    if (val != null && val.length() > 0) {
                        Matcher m = PROFILE_URI_PATTERN.matcher(val);
                        if (m.matches()) {
                            URI o = conn.getValueFactory().createURI(m.group(1));
                            conn.add(u,p,o,u);
                        } else {
                            Literal o = conn.getValueFactory().createLiteral(val.trim());
                            conn.add(u,p,o,u);
                        }
                    }
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            // This must not happen!
            return Response.serverError().entity(e).build();
        }

        return getUser(login);
    }

}
