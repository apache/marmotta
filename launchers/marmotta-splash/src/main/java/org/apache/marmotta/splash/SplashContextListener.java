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

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Listen to startup phases of individual contexts. Needs to be configured in server.xml
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SplashContextListener extends SplashScreenUpdaterBase implements LifecycleListener {

    private static Map<String,String> appNames = new HashMap<String, String>();
    static {
        appNames.put("/Marmotta", "Apache Marmotta");
    }

    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event LifecycleEvent that has occurred
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        if(event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
            if(event.getLifecycle() instanceof StandardContext) {
                StandardContext context = (StandardContext)event.getLifecycle();

                if(appNames.get(context.getName()) != null) {
                    showStatus("Starting "+appNames.get(context.getName()));
                }
            }
        }
    }
}
