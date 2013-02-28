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
package org.apache.marmotta.platform.core.test.base;

import org.apache.marmotta.platform.core.servlet.MarmottaResourceFilter;
import org.apache.marmotta.platform.core.test.base.jetty.TestApplication;
import org.apache.marmotta.platform.core.test.base.jetty.TestInjectorFactory;
import org.apache.marmotta.platform.core.util.CDIContext;
import org.apache.marmotta.platform.core.webservices.CoreApplication;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An extended version of the EmbeddedLMF which also starts a jetty webcontainer. The context name and port
 * are passed in the constructor. The JettyLMF can optionally take a set of web service classes as argument.
 * If this argument is present, only the given web services will be instantiated; otherwise, all configured
 * web services will be instantiated (as in a normal LMF webapp installation).
 * <p/>
 * Author: Sebastian Schaffert
 */
public class JettyMarmotta extends AbstractMarmotta {

    private Server jetty;

    public JettyMarmotta(String context, int port) {
        this(context, port, (Set<Class<?>>) null);
    }

    public JettyMarmotta(String context, int port, Class<?> webservice) {
        this(context,port, Collections.<Class<?>>singleton(webservice));
    }

    public JettyMarmotta(String context, int port, Class<?>... webservices) {
        this(context,port, new HashSet<Class<?>>(Arrays.asList(webservices)));
    }


    public JettyMarmotta(String context, int port, Set<Class<?>> webservice) {
        super();

        // create a new jetty
        jetty = new Server();

        // run it on port 8080
        Connector connector=new SelectChannelConnector();
        connector.setPort(port);
        jetty.setConnectors(new Connector[]{connector});


        TestInjectorFactory.setManager(container.getBeanManager());

        Context ctx = new Context(jetty,context != null ? context : "/");

        // now we have a context, start up the first phase of the LMF initialisation
        startupService.startupConfiguration(home.getAbsolutePath(),override,ctx.getServletContext());

        // register the RestEasy CDI injector factory
        ctx.setAttribute("resteasy.injector.factory", TestInjectorFactory.class.getCanonicalName());

        // register filters
        FilterHolder resourceFilter = new FilterHolder(CDIContext.getInstance(MarmottaResourceFilter.class));
        resourceFilter.setInitParameter("kiwi.resourceCaching", "true");
        ctx.addFilter(resourceFilter,"/*", Handler.DEFAULT);

        // register RestEasy so we can run web services

        // if a single web service is given, only register that webservice, otherwise startup the default configuration
        //FilterHolder restEasyFilter = new FilterHolder(org.jboss.resteasy.plugins.server.servlet.FilterDispatcher.class);
        ServletHolder restEasyFilter  = new ServletHolder(org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.class);
        restEasyFilter.setInitParameter("resteasy.injector.factory", TestInjectorFactory.class.getCanonicalName());


        if(webservice != null) {
            TestApplication.setTestedWebServices(webservice);
            //restEasyFilter.setInitParameter("resteasy.resources", webservice.getName());
            restEasyFilter.setInitParameter("javax.ws.rs.Application", TestApplication.class.getCanonicalName());
        } else {
            restEasyFilter.setInitParameter("javax.ws.rs.Application", CoreApplication.class.getCanonicalName());
        }

        //ctx.addFilter(restEasyFilter,"/*", Handler.ALL);
        ctx.addServlet(restEasyFilter, "/*");

        try {
            jetty.start();

            String url = "http://localhost:"+port+ (context != null ? context + "/" : "/");

            startupService.startupHost(url,url);
        } catch (Exception e) {
            log.error("could not start up embedded jetty server",e);
        }
    }

    @Override
    public void shutdown() {
        try {
            jetty.stop();
        } catch (Exception e) {
            log.error("could not shutdown embedded jetty server",e);
        }
        super.shutdown();
    }

}
