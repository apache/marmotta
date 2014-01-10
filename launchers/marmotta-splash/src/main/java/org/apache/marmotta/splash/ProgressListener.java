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
package org.apache.marmotta.splash;

import java.io.File;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.HostConfig;
import org.apache.marmotta.splash.common.MarmottaContext;

/**
 * Splash screen progress listener
 * 
 * @author Sebastian Schaffert
 */
public class ProgressListener extends SplashScreenUpdaterBase implements LifecycleListener, ContainerListener {

    int cur_progress = 0;
    int max_progress = 0;

    private StandardHost host;

    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event LifecycleEvent that has occurred
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        if(event.getType().equals(Lifecycle.BEFORE_INIT_EVENT)) {

            // register our custom context implementation
            host = (StandardHost)event.getLifecycle();

            for(LifecycleListener listener : host.findLifecycleListeners()) {
                if(listener instanceof HostConfig) {
                    ((HostConfig)listener).setContextClass(MarmottaContext.class.getCanonicalName());
                }
            }

        } else if(event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
            if(event.getLifecycle() instanceof Host) {
                // register a container listener to indicate when a component has started
                log.info("starting host instance");

                host = (StandardHost)event.getLifecycle();
                host.addContainerListener(this);

                File file = new File(System.getProperty("catalina.base"),host.getAppBase());

                max_progress = (file.list() != null ? file.list().length : 0);
            }
        }
    }

    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event ContainerEvent that has occurred
     */
    @Override
    public void containerEvent(ContainerEvent event) {
        if(event.getType().equals(Container.ADD_CHILD_EVENT)) {
            if(event.getData() instanceof StandardContext) {
                //StandardContext context = (StandardContext)event.getData();
                cur_progress++;
                if(max_progress > 0) {
                    showProgress(cur_progress * 100 / max_progress);
                }

            }
        }
    }
}
