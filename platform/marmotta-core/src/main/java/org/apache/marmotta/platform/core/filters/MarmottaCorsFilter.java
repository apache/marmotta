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

package org.apache.marmotta.platform.core.filters;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.modules.MarmottaHttpFilter;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.util.CorsHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@ApplicationScoped
public class MarmottaCorsFilter implements MarmottaHttpFilter {

    public static Map<String,Object> options;

    @Inject
    ConfigurationService configurationService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        initializeOptions();
    }

    public void configChanged(@Observes ConfigurationChangedEvent event) {
        if (event.getKeys().contains("kiwi.allow_origin")
                || event.getKeys().contains("kiwi.allow_methods")) {
            initializeOptions();
        }
    }

    private void initializeOptions() {
        options = new HashMap<String,Object>();
        options.put("Access-Control-Allow-Origin",configurationService.getStringConfiguration("kiwi.allow_origin","*"));
        options.put("Access-Control-Allow-Methods", configurationService.getStringConfiguration("kiwi.allow_methods","POST, PUT, GET, DELETE, HEAD"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(response instanceof HttpServletResponse) {
            HttpServletResponse resp = (HttpServletResponse)response;
            HttpServletRequest req  = (HttpServletRequest)request;

            String origin = req.getHeader("Origin");

            if(origin != null) {
                CorsHandler.run(req,resp,options);
            }

            if(req.getMethod().equalsIgnoreCase("OPTIONS")) {
                resp.setStatus(200);
            }
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {
        //
    }

    @Override
    public String getPattern() {
        return "^/.*";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
