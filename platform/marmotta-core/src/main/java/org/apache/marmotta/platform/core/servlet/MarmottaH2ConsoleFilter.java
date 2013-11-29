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

import org.apache.marmotta.platform.core.api.config.ConfigurationService;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This filter injects the database configuration into the H2 console using the LMF Configuration settings
 * <p/>
 * User: sschaffe
 */
public class MarmottaH2ConsoleFilter implements Filter {

    @Inject
    private ConfigurationService configurationService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {


        if(servletRequest instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest)servletRequest;

            if(req.getPathInfo() != null && req.getPathInfo().endsWith("login.jsp")) {
                // we redirect immediately to the action, since the login screen is not needed
                String origUri = req.getRequestURI();
                String redirectUri = origUri.substring(0,origUri.lastIndexOf("/")) + "/login.do?jsessionid="+req.getParameter("jsessionid");

                ((HttpServletResponse)servletResponse).sendRedirect(redirectUri);
            }

            if(req.getPathInfo() != null && req.getPathInfo().endsWith("login.do")) {
                // set the database configuration from system configuration
                String db_type = configurationService.getStringConfiguration("database.type","h2");
                String db_driver = configurationService.getStringConfiguration("database."+db_type+".driver");
                String db_url =  configurationService.getStringConfiguration("database.url");
                String db_user =  configurationService.getStringConfiguration("database.user");
                String db_password =  configurationService.getStringConfiguration("database.password");

                servletRequest.setAttribute("driver",db_driver);
                servletRequest.setAttribute("url",db_url);
                servletRequest.setAttribute("user",db_user);
                servletRequest.setAttribute("password",db_password);
            }
        }

        filterChain.doFilter(servletRequest,servletResponse);


    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
