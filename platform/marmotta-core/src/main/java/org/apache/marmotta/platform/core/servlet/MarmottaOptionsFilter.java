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

import org.jboss.resteasy.core.NoMessageBodyWriterFoundFailure;
import org.jboss.resteasy.spi.DefaultOptionsMethodException;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * This filter checks for OPTIONS requests. If the response returned by the other filters throws an exception
 * org.jboss.resteasy.spi.DefaultOptionsMethodException, the filter writes to the response the default options
 * of the Apache Marmotta system.
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public class MarmottaOptionsFilter implements Filter {

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
        ResteasyProviderFactory.getInstance().registerProviderInstance(new OptionsMapper());
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
        try {
            chain.doFilter(request,response);
        } catch (DefaultOptionsMethodException | NoMessageBodyWriterFoundFailure ex) {
            if(response instanceof HttpServletResponse) {
                HttpServletResponse resp = (HttpServletResponse)response;
                HttpServletRequest req  = (HttpServletRequest)request;
                if(req.getMethod().equalsIgnoreCase("OPTIONS")) {
                    resp.setStatus(200);
                    resp.resetBuffer();
                }
            }
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Provider
    protected static class OptionsMapper implements ExceptionMapper<DefaultOptionsMethodException> {

        @Override
        public Response toResponse(DefaultOptionsMethodException exception) {
            throw exception;
        }

    }
}
