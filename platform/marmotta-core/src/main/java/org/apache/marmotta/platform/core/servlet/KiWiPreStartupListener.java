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
package org.apache.marmotta.platform.core.servlet;

import org.apache.marmotta.platform.core.startup.LMFStartupService;
import org.apache.marmotta.platform.core.util.KiWiContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This filter is executed first in the startup chain. It initialises the Apache Marmotta system variables if necessary and
 * starts up the configuration service.
 */
public class KiWiPreStartupListener implements ServletContextListener {

    private LMFStartupService lmfStartupService;

    /**
     * * Notification that the web application initialization
     * * process is starting.
     * * All ServletContextListeners are notified of context
     * * initialization before any filter or servlet in the web
     * * application is initialized.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        if(lmfStartupService == null) {
            lmfStartupService = KiWiContext.getInstance(LMFStartupService.class);
        }

        lmfStartupService.startupConfiguration(null,null,sce.getServletContext());

    }

    /**
     * * Notification that the servlet context is about to be shut down.
     * * All servlets and filters have been destroy()ed before any
     * * ServletContextListeners are notified of context
     * * destruction.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        lmfStartupService.shutdown();
    }

}
