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
package org.apache.marmotta.platform.core.servlet;

import org.apache.marmotta.platform.core.startup.MarmottaStartupService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This filter is evaluated when the Apache Marmotta system is accessed for the very first time using an HTTP client (e.g. a
 * browser). Its purpose is to set configuration variables that cannot be determined when the server is starting
 * up because they need information how the server is accessed. In particular, it will set the following
 * configuration variables in case the system has not yet been configured:
 * <ul>
 *  <li>kiwi.context - will be set to the base URI of this KiWi installation; used for constructing resource URIs</li>
 *  <li>kiwi.host    - will be set to the base URL of the KiWi installation; used for accessing additional web services like SOLR and H2</li>
 * </ul>
 * <p/>
 * User: Sebastian Schaffert
 */
public class MarmottaPreStartupFilter implements Filter {

    @Inject
    private Logger log;

    @Inject
    private MarmottaStartupService startupService;

    private boolean started = false;

    /**
     * If the hostname (config properties kiwi.host and kiwi.context) of the system has not been configured yet, we
     * try to automatically determine the correct values from the first request. The config variable
     * kiwi.setup.host indicates whether setup has already been carried out.
     * @param request
     */
    private String getBaseUrl(ServletRequest request) {
        // check whether we need to perform setup
        if (request instanceof HttpServletRequest) {
            HttpServletRequest hreq = (HttpServletRequest) request;

            String contextName = hreq.getContextPath();
            String hostName    = hreq.getServerName();
            int hostPort       = hreq.getServerPort();
            String hostScheme  = hreq.getScheme();

            String baseUrl = hostScheme + "://" + hostName;
            if( hostPort == 80 && "http".equals(hostScheme) ||
                    hostPort == 443 && "https".equals(hostScheme)  ) {
                baseUrl = baseUrl + contextName + "/";
            } else {
                baseUrl = baseUrl + ":" + hostPort + contextName + "/";
            }

            return baseUrl;
        } else {
            return null;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if(!started) {
            synchronized (this) {
                started = true;

                String baseUrl = getBaseUrl(request);
                if(baseUrl != null) {
                    startupService.startupHost(baseUrl, baseUrl);
                } else {
                    log.error("could not determine host name; cannot startup Apache Marmotta");
                }
            }
        }

        filterChain.doFilter(request,servletResponse);
    }


    @Override
    public void destroy() {

    }
}
