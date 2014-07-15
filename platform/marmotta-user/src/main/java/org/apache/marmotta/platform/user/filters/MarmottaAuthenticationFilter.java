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
package org.apache.marmotta.platform.user.filters;

import org.apache.marmotta.platform.core.api.modules.MarmottaHttpFilter;
import org.apache.marmotta.platform.user.api.AuthenticationService;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.exception.UserExistsException;
import org.apache.marmotta.platform.core.exception.security.AccessDeniedException;
import org.jboss.resteasy.spi.UnhandledException;
import org.openrdf.model.URI;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.commons.codec.binary.Base64.decodeBase64;

/**
 * This filter implements HTTB Basic Authentication support for the Marmotta. It serves two purposes:
 * <ul>
 *     <li>carrying out HTTP Basic Authentication when an "Authorization" header is present</li>
 *     <li>sending an HTTP authorization request in case one of the services in the chain threw an AccessDeniedException</li>
 * </ul>
 *
 * In case security is enabled, it listens for the presence of an "Authorization" header, parses it according to HTTP Basic Authentication
 * (Base64 decoding and splitting username/password at ":") and tries to authenticate with the given
 * credentials using the Marmotta AuthenticationService.
 * <ul>
 * <li>In case authentication succeeds, it sets the attributes
 *   user.name and user.roles in the request so that further filters/services can make use of the
 *   authentication information, and it sets the current user for all activities carried out in the thread.</li>
 * <li>In case authentication fails, no user information is added to the request.</li>
 * </ul>
 * The filter also listens for {@link org.apache.marmotta.platform.core.exception.security.AccessDeniedException} thrown by
 * subsequent filters or servlets in the chain, in which case it returns an HTTP authorization request to the
 * client. In particular, this functionality is used by the MarmottaAccessControlFilter to restrict access to
 * services based on security profiles.
 * <p/>
 * @see UserService
 * 
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class MarmottaAuthenticationFilter implements MarmottaHttpFilter {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private UserService userService;

    @Inject
    private AuthenticationService authenticationService;

    /**
     * Initialise authentication filter
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("armotta Authentication Filter starting up; user authentication is {}", configurationService.getBooleanConfiguration("security.enabled", true)?"enabled":"disabled");
    }


    /**
     * Return the pattern (regular expression) that a request URI (relative to the base URI) has to match
     * before triggering this filter.
     *
     * @return
     */
    @Override
    public String getPattern() {
        return "^/.*";
    }

    /**
     * Return the priority of the filter. Filters that need to be executed before anything else should return
     * PRIO_FIRST, filters that need to be executed last in the chain should return PRIO_LAST, all other filters
     * something inbetween (e.g. PRIO_MIDDLE).
     *
     * @return
     */
    @Override
    public int getPriority() {
        return PRIO_AUTH;
    }

    /**
     * Check for the presence of a "Authorization" header in the request header and authorize the user if yes.
     * Sets the attributes "user.name" and "user.roles" in the request for further processing.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(configurationService.getBooleanConfiguration("security.enabled",true)) {

            HttpServletRequest httpRequest = (HttpServletRequest)request;

            // check whether authentication information has been sent with the request

            try {
                String authorization = httpRequest.getHeader("Authorization");
                if (authorization != null) {
                    boolean authSuccess = false;
                    String login = Namespaces.ANONYMOUS_LOGIN;
                    if (authorization.startsWith("Basic ")) {
                        String credentialsBase64 = authorization.split(" ")[1];
                        String credentialsPlain = new String(decodeBase64(credentialsBase64));
                        String[] credentials = credentialsPlain.split(":");
                        if (credentials.length == 2) {
                            login = credentials[0];
                            String passwd = credentials[1];

                            if (authenticationService.authenticateUser(login, passwd)) {
                                authSuccess = true;
                                httpRequest.setAttribute("user.name", login);
                                httpRequest.setAttribute("user.roles", authenticationService.listUserRoles(login));
                                URI user = userService.getUser(login);
                                if (user == null) {
                                    try {
                                        user = userService.createUser(login);
                                    } catch (UserExistsException e) {
                                    }
                                }
                                userService.setCurrentUser(user);
                            }
                        }
                    }
                    if (!authSuccess && !login.equals(Namespaces.ANONYMOUS_LOGIN)) {
                        // Apparently wrong username/passwd: ask for the correct one
                        throw new AccessDeniedException();
                    }
                }

                chain.doFilter(request,response);
            } catch(AccessDeniedException ex) {
                build401Response(response);
            } catch (UnhandledException ue) {
                // This is to handle AccessDeniedExeptions in REST-Webservices
                if (ue.getCause().getClass().equals(AccessDeniedException.class)) {
                    build401Response(response);
                } else
                    throw ue;
            } finally {
                userService.clearCurrentUser();
            }
        } else {
            chain.doFilter(request,response);
        }
    }

    private void build401Response(ServletResponse response) {
        // access denied; request authentication
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String authMethod = "Basic";
        if ("BASIC".equalsIgnoreCase(configurationService.getStringConfiguration("security.method", authMethod))) {
            authMethod = "Basic";
            //                } else if("DIGEST".equalsIgnoreCase(configurationService.getStringConfiguration("security.method"))) {
            //                    authMethod = "Digest";
        }
        String authRealm  = configurationService.getStringConfiguration("security.realm","Apache Marmotta");

        httpResponse.setHeader("WWW-Authenticate",authMethod + " realm=\""+authRealm+"\"");
    }

    /**
     * Destroy authentication filter
     */
    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
