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
package org.apache.marmotta.splash.common;

import org.apache.catalina.core.StandardContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.marmotta.splash.SplashContextListener;
import org.apache.marmotta.splash.systray.SystrayListener;


/**
 * A customised context implementation that adds a message to the splash screen when it is starting by registering
 * a context listener.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class MarmottaContext extends StandardContext {

    protected static Log log = LogFactory.getLog(MarmottaContext.class);

    public MarmottaContext() {
        super();

        // add the splash listener so the progress bar can be updated by this context
        addLifecycleListener(new SplashContextListener());

        // register Marmotta context in systray listener, the systray listener will check for menu entries stored
        // in the context attributes "systray.admin" and "systray.demo"
        SystrayListener.addServletContext(this);

        log.info("instantiated new Marmotta/Tomcat webapp context ("+getName()+")...");
    }
}
