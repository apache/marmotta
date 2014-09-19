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
import org.apache.marmotta.platform.core.api.modules.MarmottaHttpFilter;
import org.apache.marmotta.platform.core.api.modules.ModuleService;
import org.jboss.resteasy.spi.BadRequestException;
import org.slf4j.Logger;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
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
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * This filter is used by KiWi for initialisation of the KiWi system on startup of the server. It does not perform
 * any actual filtering. For this purpose, a listener would have been better, but CDI in Jetty does not support
 * injection into listeners, so we "abuse" a filter for this purpose. Filters always get initialised before servlets,
 * so by adding the KiWiFilter as the first entry into web.xml, we can ensure that the KiWi initialisation is done
 * before everything else.
 * <p/>
 * User: sschaffe
 */
public class MarmottaResourceFilter implements Filter {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ModuleService moduleService;

    @Inject @Any
    private Instance<MarmottaHttpFilter> filters;

    private List<MarmottaHttpFilter> filterList;

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
        log.info("Apache Marmotta Resource Filter {} starting up ... ", configurationService.getConfiguration("kiwi.version"));

        // initialise filter chain and sort it according to priority
        this.filterList  = new ArrayList<MarmottaHttpFilter>();

        for(MarmottaHttpFilter filter : filters) {
            try {
                filter.init(filterConfig);
                filterList.add(filter);

                log.debug("module {}: registered filter {}", moduleService.getModuleConfiguration(filter.getClass()).getModuleName(), filter.getClass().getCanonicalName());
            } catch (ServletException ex) {
                log.error("could not instantiate filter {}, servlet exception during initialisation ({})",filter.getClass(),ex.getMessage());
            }
        }

        Collections.sort(filterList,new FilterComparator());
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
        String prefix, path = null;
        if (request instanceof HttpServletRequest) {
            url = new URL(((HttpServletRequest)request).getRequestURL().toString());
            prefix = ((HttpServletRequest)request).getContextPath();
            if(url.getPath().startsWith(prefix)) {
                path = url.getPath().substring(prefix.length());
            }
        }

        try {
            new MarmottaFilterChain(path, chain).doFilter(request, response);
        } catch (BadRequestException e) {
            ((HttpServletResponse)response).sendError(400, e.getMessage());
        }
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
        for(MarmottaHttpFilter filter : filterList) {
            filter.destroy();
        }
    }

    /**
     * A special filter chain that implements the FilterChain calls
     */
    private class MarmottaFilterChain implements FilterChain {

        private Iterator<MarmottaHttpFilter> filters;

        private String path;

        private FilterChain originalChain;

        MarmottaFilterChain(String path, FilterChain originalChain) {
            this.path     = path;
            this.filters = filterList.iterator();
            this.originalChain = originalChain;
        }


        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            if(filters.hasNext()) {
                MarmottaHttpFilter filter = filters.next();

                if(path.matches(filter.getPattern())) {
                    filter.doFilter(request,response,this);
                } else {
                    doFilter(request,response);
                }
            } else {
                originalChain.doFilter(request,response);
            }
        }
    }



    private static class FilterComparator implements Comparator<MarmottaHttpFilter>, Serializable {

        private static final long serialVersionUID = -7264645592168345092L;

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         * <p/>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is less than, equal to, or greater than the
         *         second.
         * @throws ClassCastException if the arguments' types prevent them from
         *                            being compared by this comparator.
         */
        @Override
        public int compare(MarmottaHttpFilter o1, MarmottaHttpFilter o2) {
            if(o1.getPriority() < o2.getPriority())
                return -1;
            else if(o1.getPriority() > o2.getPriority()) return 1;
            else
                return 0;
        }
    }
}
