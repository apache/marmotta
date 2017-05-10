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

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.security.AccessDeniedException;
import org.apache.marmotta.platform.user.api.AccountService;
import org.apache.marmotta.platform.user.model.UserAccount;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.REFERER;

/**
 * User-Account related webservices, accessable by every user (each for his/her own data)
 *
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 *
 */
@Path("/user")
public class UserWebService {

    private static final Pattern PROFILE_URI_PATTERN = Pattern.compile("^<([^>]+)>$");

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private UserService          userService;

    @Inject
    private AccountService       accountService;

    @Inject
    private SesameService        sesameService;

    private List<String>         acceptedFoafProperties;

    @PostConstruct
    public void initialize() {
        acceptedFoafProperties = configurationService.getListConfiguration("user.account.foaf.properties",
                Arrays.asList("firstName", "nick", "lastName", "familyName", "givenName", "name", "title", "age", "mbox", "homepage"));
    }

    /**
     * Provide Account information about the currently logged in user (account)
     *
     * @return {@link AccountPoJo} of the current user in JSON
     * @see AccountPoJo
     */
    @GET
    @Path("/me")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public Response get() {
        return get(userService.getCurrentUser());
    }

    private Response get(URI user) {
        if (userService.isAnonymous(user)) {
            AccountPoJo apj = new AccountPoJo(Namespaces.ANONYMOUS_LOGIN, user.stringValue());
            return Response.ok(apj, Namespaces.MIME_TYPE_JSON).location(java.net.URI.create(user.stringValue())).build();
        }
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                final UserAccount a = accountService.getAccount(user);
                if (a != null) {
                    AccountPoJo apj = new AccountPoJo(a.getLogin(), a.getWebId());
                    apj.setRoles(a.getRoles());

                    for (Statement t : ResourceUtils.listOutgoing(conn,conn.getValueFactory().createURI(a.getWebId()))) {
                        String prop = t.getPredicate().stringValue();
                        if (prop.startsWith(Namespaces.NS_FOAF)) {
                            Value object = t.getObject();
                            if (object instanceof org.openrdf.model.URI) {
                                apj.setFoaf(prop, String.format("<%s>", object));
                            } else if (object instanceof Literal) {
                                apj.setFoaf(prop, object.toString());
                            }
                        }
                    }

                    return Response.ok(apj, Namespaces.MIME_TYPE_JSON).location(java.net.URI.create(user.stringValue())).build();
                }
                return Response.status(Status.NOT_FOUND).entity("Could not find account data of " + user).build();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            // This must not happen!
            return Response.serverError().entity(e).build();
        }

    }

    /**
     * Update/Set the profile information (foaf) for the current user.
     * Post-Body should contain the property=value mapping (propterty without foaf-prefix) for the
     * profile.
     *
     * @param formParams the user profile (foaf, without prefix) in {@value Namespaces#MIME_TYPE_FORM_URLENC}
     * @return {@link AccountPoJo} after the update in JSON
     *
     * @HTTP 403 When the current user is <code>anonymous</code>.
     * @HTTP 500 If a {@link RepositoryException} occurs (which should not happen as no
     *       namespaces are used here)
     */
    @POST
    @Path("/me")
    @Consumes(Namespaces.MIME_TYPE_FORM_URLENC)
    public Response post(MultivaluedMap<String, String> formParams) {
        final URI currentUser = userService.getCurrentUser();
        if (userService.isAnonymous(currentUser)) return Response.status(Status.FORBIDDEN).entity("anonymous is read-only").build();

        try {
            RepositoryConnection conn = sesameService.getConnection();

            try {
                for (String prop : formParams.keySet()) {
                    if (!acceptedFoafProperties.contains(prop)) {
                        continue;
                    }
                    URI p = conn.getValueFactory().createURI(Namespaces.NS_FOAF + prop);

                    conn.remove(currentUser,p,null);

                    String val = formParams.getFirst(prop);
                    if (val != null && val.length() > 0) {
                        Matcher m = PROFILE_URI_PATTERN.matcher(val);
                        if (m.matches()) {
                            URI o = conn.getValueFactory().createURI(m.group(1));
                            conn.add(currentUser, p, o, currentUser);
                        } else {
                            Literal o = conn.getValueFactory().createLiteral(val.trim());
                            conn.add(currentUser, p, o, currentUser);
                        }
                    }

                }
                return get(currentUser);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException e) {
            // This must not happen!
            return Response.serverError().entity(e).build();
        }
    }

    /**
     * Dummy to avoid exceptions if post body is empty.
     *
     * @see #post(MultivaluedMap)
     * @return {@link AccountPoJo} of the current user in JSON
     */
    @POST
    @Path("/me")
    public Response post() {
        return get();
    }

    /**
     * Update/change the password for the current user.
     *
     * @param oldPwd the old (current) password.
     * @param newPwd the new password
     * @return 200 OK on success
     * @HTTP 404 if the current account could not be loaded
     * @HTTP 403 if the old pasword did not match
     */
    @POST
    @Path("/me/passwd")
    public Response passwd(@FormParam("oldPasswd") String oldPwd, @FormParam("newPasswd") String newPwd) {
        final org.openrdf.model.URI currentUser = userService.getCurrentUser();
        final UserAccount a = accountService.getAccount(currentUser);

        if (a == null) return Response.status(Status.NOT_FOUND).entity(String.format("No account found for <%s>", currentUser)).build();

        if (accountService.checkPassword(a, oldPwd)) {
            accountService.setPassword(a, newPwd);
            return Response.ok("Password changed").build();
        } else
            return Response.status(Status.FORBIDDEN).entity("password check failed").build();
    }

    /**
     * Resolve/Redirect access to /user/* uris.
     *
     * @param login the login of the user to redirect to
     * @param types header param of accepted mime-types
     * @return a redirect to the user-resource in the resource service.
     * @HTTP 404 if no such user exists.
     * @HTTP 303 on success
     * @HTTP 400 if no valid resource uri could be built with the login
     * @HTTP 500 on other exceptions
     */
    //@GET
    //@Path("/{login:[^#?]+}")
    public Response getUser(@PathParam("login") String login, @HeaderParam("Accept") String types) {
        if(login.equals("me")) {
            return get();
        } else {
            try {
                RepositoryConnection conn = sesameService.getConnection();
                try {
                    final URI user = userService.getUser(login);
                    if (user == null) return Response.status(Status.NOT_FOUND).entity(String.format("User %s not found", login)).build();

                    java.net.URI u = new java.net.URI(configurationService.getServerUri() + "resource?uri=" + URLEncoder.encode(user.stringValue(), "utf-8"));

                    return Response.seeOther(u).header(ACCEPT, types).build();
                } finally {
                    conn.commit();
                    conn.close();
                }
            } catch (URISyntaxException e) {
                return Response.status(Status.BAD_REQUEST).entity(String.format("Invalid URI: %s", e.getMessage())).build();
            } catch (UnsupportedEncodingException e) {
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            } catch (RepositoryException e) {
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
        }
    }

    /**
     * Throws a {@link AccessDeniedException} if currently no user is logged in
     * (aka: current user is anonymous).
     *
     * @param ref the referer to redirect to
     * @param logout set to true to log out (does currently nothing)
     * @return a redirect to the referer url
     * @throws AccessDeniedException if currently no user is logged in.
     * @HTTP 303 if the user is already logged in (or <code>logout == true</code>)
     */
    @GET
    @Path("/login")
    public Response login(@HeaderParam(REFERER) String ref, @QueryParam("logout") @DefaultValue("false") boolean logout,
                          @QueryParam("user") String login) {
        // Check whether we want to logout
        if (logout) {
            userService.setCurrentUser(userService.getAnonymousUser());
            throw new AccessDeniedException();
        }

        // Anonymous cannot login
        if (userService.isAnonymous(userService.getCurrentUser())) throw new AccessDeniedException();

        // Check whether this is the right (desired) user
        if (login != null && !userService.getCurrentUser().equals(userService.getUser(login))) throw new AccessDeniedException();

        if (ref == null || "".equals(ref)) {
            ref = configurationService.getServerUri() + configurationService.getStringConfiguration("kiwi.pages.startup");
        }
        return Response.seeOther(java.net.URI.create(ref)).build();
    }

    /**
     * Wrapped AccountInformation for serialisation.
     *
     * @author Jakob Frank <jakob.frank@salzburgresearch.at>
     *
     */
    static class AccountPoJo {
        private String login, uri, roles[];
        private Map<String, String> foaf;

        public AccountPoJo(String login, String uri) {
            this.login = login;
            if (uri != null) {
                this.uri = uri;
            } else {
                this.uri = null;
            }
            this.roles = new String[0];
            this.foaf = new HashMap<String, String>();
        }

        public void setRoles(Set<String> roles) {
            if (roles != null) {
                this.roles = roles.toArray(new String[roles.size()]);
            } else {
                this.roles = new String[0];
            }
        }

        public void setFoaf(String prop, String value) {
            foaf.put(prop, value);
        }

        public void setFoaf(Map<String, String> foaf) {
            this.foaf = foaf;
        }

        public String getLogin() {
            return login;
        }

        public String getUri() {
            return uri;
        }

        public String[] getRoles() {
            return roles;
        }

        public Map<String, String> getFoaf() {
            return foaf;
        }

    }

}
