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
package org.apache.marmotta.platform.versioning.filter;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.modules.MarmottaHttpFilter;
import org.apache.marmotta.platform.versioning.utils.MementoUtils;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds memento timegate links to resources that are delivered via ResourceWebService
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@ApplicationScoped
public class MementoFilter implements MarmottaHttpFilter {

    //filter all requests on resource webservice
    private static final String FILTER_PATTERN = "/(" + ConfigurationService.RESOURCE_PATH + "|" +
            ConfigurationService.META_PATH + "|" +
            ConfigurationService.CONTENT_PATH +")/.*";

    @Inject
    private Logger log;

    @Inject
    ConfigurationService configurationService;

    @Override
    public String getPattern() {
        return FILTER_PATTERN;
    }

    @Override
    public int getPriority() {
        return PRIO_MIDDLE;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //nothing to do
        log.info("init memento filter");
    }

    /**
     * appends memento timegate link to all resources, that are accessed via GET on resource webservice
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;

            //memento is only for reading
            if(request.getMethod().equals("GET") || request.getMethod().equals("HEAD")) {
                HttpServletResponse response = (HttpServletResponse) servletResponse;

                URL resource = getResourceURL(request);

                // build timegate link
                if(resource != null) {
                    response.addHeader("Link",
                            "<"+MementoUtils.timegateURI(
                                   resource.toString(),configurationService.getBaseUri()
                            ) +">; rel=\"timegate\"," +
                            "<"+MementoUtils.timemapURI(
                                    resource.toString(),configurationService.getBaseUri()
                            ) +">; rel=\"timemap\""
                    );
                }

                filterChain.doFilter(servletRequest,response);
                return;
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    /**
     * returns the resource url for a request, null if resource does not exist
     * @param request
     * @return
     */
    private URL getResourceURL(HttpServletRequest request) {
        try {
            if(request.getParameter("uri") != null) {
                //memento is only for http protocol
                if(request.getParameter("uri").startsWith("http://") || request.getParameter("uri").startsWith("https://")) {
                    return new URL(request.getParameter("uri"));
                }
            } else {
                //test if it is a resource / meta / content url
                if(request.getRequestURI().startsWith("/"+ ConfigurationService.RESOURCE_PATH+"/")) {
                    return new URL(request.getRequestURL().toString());
                } else if(request.getRequestURI().startsWith("/"+ ConfigurationService.CONTENT_PATH+"/") ||
                            request.getRequestURI().startsWith("/"+ ConfigurationService.META_PATH+"/")) {
                    return new URL(restoreResource(request.getRequestURI()));
                }
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private static final Pattern URI_PATTERN = Pattern.compile("(/[^/]*){3}/(.*)");

    /**
     * restore uri from meta or content url
     * @param uri
     * @return
     * @throws MalformedURLException
     */
    private String restoreResource(String uri) throws MalformedURLException {
        Matcher m = URI_PATTERN.matcher(uri);
        if (m.matches()) return configurationService.getBaseUri() + ConfigurationService.RESOURCE_PATH + "/" + m.group(2);
        throw new MalformedURLException("original url can not be restored");
    }

    @Override
    public void destroy() {
        //nothing to do
    }
}
