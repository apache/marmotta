/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.lmf.common;

import org.apache.catalina.core.StandardContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import at.newmedialab.lmf.splash.SplashContextListener;
import at.newmedialab.lmf.systray.SystrayListener;

/**
 * A customised context implementation that adds a message to the splash screen when it is starting by registering
 * a context listener.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LMFContext extends StandardContext {

    protected static Log log = LogFactory.getLog(LMFContext.class);

    public LMFContext() {
        super();

        // add the splash listener so the progress bar can be updated by this context
        addLifecycleListener(new SplashContextListener());

        // register LMF context in systray listener, the systray listener will check for menu entries stored
        // in the context attributes "systray.admin" and "systray.demo"
        SystrayListener.addServletContext(this);

        log.info("instantiated new LMF/Tomcat webapp context ("+getName()+")...");
    }
}
