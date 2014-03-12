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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.marmotta.platform.core.startup.MarmottaStartupService;
import org.apache.marmotta.platform.core.util.CDIContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This listener is executed first in the startup chain, initializing the Apache 
 * Marmotta system variables if necessary and starting up the configuration service
 * 
 * @author Sebastian Schaffert
 */
public class MarmottaPreStartupListener implements ServletContextListener {

    private static Logger log = LoggerFactory.getLogger(MarmottaPreStartupListener.class);

    private MarmottaStartupService startupService;

    /**
     * * Notification that the web application initialization
     * * process is starting.
     * * All ServletContextListeners are notified of context
     * * initialization before any filter or servlet in the web
     * * application is initialized.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        if(startupService == null) {
            startupService = CDIContext.getInstance(MarmottaStartupService.class);
        }

        // we check for the presence of the configuration.override init parameter; if it exists, we load this
        // configuration file and pass it as configuration override to the startup
        PropertiesConfiguration override = null;

        if(sce.getServletContext().getInitParameter("configuration.override") != null) {
            try {
                override = new PropertiesConfiguration(sce.getServletContext().getInitParameter("configuration.override"));
            } catch (ConfigurationException e) {
                log.warn("could not load configuration override file from {}", sce.getServletContext().getInitParameter("configuration.override"));
            }
        }

        startupService.startupConfiguration(null, override, sce.getServletContext());

    }

    /**
     * * Notification that the servlet context is about to be shut down.
     * * All servlets and filters have been destroy()ed before any
     * * ServletContextListeners are notified of context
     * * destruction.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        startupService.shutdown();
    }

}
