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
package org.apache.marmotta.platform.core.filters;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.modules.MarmottaHttpFilter;
import org.apache.marmotta.platform.core.api.modules.MarmottaResourceService;
import org.apache.marmotta.platform.core.api.modules.ResourceEntry;
import org.jboss.resteasy.spi.BadRequestException;
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
import java.net.URL;

/**
 * Resolve the resources of LMF modules and deliver them. Proceeds with the chain if not found.
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class ModuleResourceFilter implements MarmottaHttpFilter {

    @Inject
    private Logger log;

    @Inject
    private MarmottaResourceService resourceService;

    @Inject
    private ConfigurationService configurationService;

    /**
     * Return the pattern (regular expression) that a request URI (relative to the LMF base URI) has to match
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
        return PRIO_LAST;
    }

    /**
     * Called by the web container to indicate to a filter that it is being placed into
     * service. The servlet container calls the init method exactly once after instantiating the
     * filter. The init method must complete successfully before the filter is asked to do any
     * filtering work. <br><br>
     * <p/>
     * The web container cannot place the filter into service if the init method either<br>
     * 1.Throws a ServletException <br>
     * 2.Does not return within a time period defined by the web container
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a request/response pair is passed through the chain due
     * to a client request for a resource at the end of the chain. The FilterChain passed in to this
     * method allows the Filter to pass on the request and response to the next entity in the
     * chain.<p>
     * A typical implementation of this method would follow the following pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering <br>
     * 3. Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering <br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using the FilterChain object (<code>chain.doFilter()</code>), <br>
     * * 4. b) <strong>or</strong> not pass on the request/response pair to the next entity in the filter chain to block the request processing<br>
     * * 5. Directly set headers on the response after invocation of the next entity in the filter chain.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        URL url = null;
        String prefix = null, path = null;
        if (request instanceof HttpServletRequest) {
            url    = new URL(((HttpServletRequest)request).getRequestURL().toString());
            prefix = ((HttpServletRequest)request).getContextPath();
            if(url.getPath().startsWith(prefix)) {
                path = url.getPath().substring(prefix.length());
            }
        }


        if(path != null) {
            long starttime = System.currentTimeMillis();

            ResourceEntry data = resourceService.getResource(path);

            // if no: proceed with the chain by calling chain.doFilter()
            if(data != null && data.getLength() > 0) {

                HttpServletResponse hresponse = (HttpServletResponse) response;

                if(configurationService.getBooleanConfiguration("resources.browsercache.enabled",true)) {
                    hresponse.setDateHeader("Expires", System.currentTimeMillis()+configurationService.getIntConfiguration("resources.browsercache.seconds",300)*1000);
                } else {
                    hresponse.setHeader("Cache-Control", "no-store");
                    hresponse.setHeader("Pragma", "no-cache");
                    hresponse.setDateHeader("Expires", 0);
                }

                if(data.getContentType() != null && !data.getContentType().contains("unknown")) {
                    hresponse.setContentType(data.getContentType());
                }
                hresponse.setContentLength(data.getLength());

                hresponse.getOutputStream().write(data.getData());
                hresponse.getOutputStream().flush();
                hresponse.getOutputStream().close();

                if (log.isDebugEnabled()) {
                    log.debug("request for {} took {}ms", url, System.currentTimeMillis() - starttime);
                }
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Called by the web container to indicate to a filter that it is being taken out of service. This
     * method is only called once all threads within the filter's doFilter method have exited or after
     * a timeout period has passed. After the web container calls this method, it will not call the
     * doFilter method again on this instance of the filter. <br><br>
     * <p/>
     * This method gives the filter an opportunity to clean up any resources that are being held (for
     * example, memory, file handles, threads) and make sure that any persistent state is synchronized
     * with the filter's current state in memory.
     */
    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
