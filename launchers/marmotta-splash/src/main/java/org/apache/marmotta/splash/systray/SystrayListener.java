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
package org.apache.marmotta.splash.systray;

import static org.apache.marmotta.splash.common.MarmottaStartupHelper.getServerName;
import static org.apache.marmotta.splash.common.MarmottaStartupHelper.getServerPort;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.marmotta.splash.common.MarmottaContext;
import org.apache.marmotta.splash.common.ui.MessageDialog;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SystrayListener implements LifecycleListener {

    protected static Log log = LogFactory.getLog(SystrayListener.class);

    private static Set<MarmottaContext>       contexts = new HashSet<MarmottaContext>();

    private TrayIcon                         icon;

    private Map<String, String>              demoLinks;

    private Map<String, String>              adminLinks;

    public static void addServletContext(MarmottaContext context) {
        contexts.add(context);
    }

    public SystrayListener() {
        super();
    }

    /**
     * Register the systray menu when the application server has successfully started.
     *
     * @param event LifecycleEvent that has occurred
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        if(event.getType().equals(Lifecycle.AFTER_START_EVENT) && SystemTray.isSupported()) {
            initContextLinks();
            initSysTray();
        }
    }

    /**
     * Check all servlet contexts for the presence of the systray.admin and systray.demo attributes and add the link
     * definitions to the menu link maps
     */
    private void initContextLinks() {
        demoLinks = new HashMap<String, String>();
        adminLinks = new HashMap<String, String>();

        for(MarmottaContext ctx : contexts) {
            if(ctx.getServletContext() != null) {
                Object ctxAdminAttr = ctx.getServletContext().getAttribute("systray.admin");
                if(ctxAdminAttr != null && ctxAdminAttr instanceof Map) {
                    @SuppressWarnings("unchecked")
					Map<String,String> ctxAdminLinks = (Map<String,String>) ctxAdminAttr;
                    adminLinks.putAll(ctxAdminLinks);
                }

                Object ctxDemoAttr = ctx.getServletContext().getAttribute("systray.demo");
                if(ctxDemoAttr != null && ctxDemoAttr instanceof Map) {
                    @SuppressWarnings("unchecked")
					Map<String,String> ctxDemoLinks = (Map<String,String>) ctxDemoAttr;
                    demoLinks.putAll(ctxDemoLinks);
                }
            } else {
                log.error("could not register systray links because servlet context is not yet initialised");
            }
        }
    }


    private void initSysTray() {

        if (SystemTray.isSupported()) {
            // allow proper shutdown
            System.setProperty("org.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES","false");


            SystemTray tray = SystemTray.getSystemTray();

            // create menu
            PopupMenu popup = new PopupMenu();


            //MenuItem mainPage = createMenuItem("Start Page", "http://"+getServerName()+":"+getServerPort()+"/");
            //popup.add(mainPage);
            //popup.addSeparator();


            // launch browser action
            MenuItem admin = createMenuItem("Administration","http://"+getServerName()+":"+getServerPort()+"/");
            popup.add(admin);

            // admin links

            for(final Map.Entry<String,String> linkEntry : adminLinks.entrySet()) {
                MenuItem entry = createMenuItem(linkEntry.getKey(),linkEntry.getValue());
                popup.add(entry);
            }

            // shutdown action
            MenuItem shutdown = new MenuItem("Shutdown");
            try {
                Class.forName("org.apache.catalina.mbeans.MBeanUtils");
                ActionListener stopListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        performServerShutdown();
                    }
                };
                shutdown.addActionListener(stopListener);
            } catch (ClassNotFoundException e) {
                shutdown.setEnabled(false);
            }
            popup.add(shutdown);

            
            if (!demoLinks.isEmpty()) {
                popup.addSeparator();
            }
            for(final Map.Entry<String,String> linkEntry : demoLinks.entrySet()) {
                boolean containsEntry = false;
                for(int i = 0; i < popup.getItemCount(); i++) {
                    MenuItem item = popup.getItem(i);
                    if(item.getLabel().equals(linkEntry.getKey())) {
                        containsEntry = true;
                        break;
                    }
                }

                if(!containsEntry) {
                    MenuItem entry = createMenuItem(linkEntry.getKey(),linkEntry.getValue());
                    popup.add(entry);
                }
            }

            popup.addSeparator();
            MenuItem about = new MenuItem("About");
            about.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MessageDialog.show("Apache Marmotta",
                            "About Apache Marmotta \n",
                            "(c)2014 The Apache Software Foundation \n" +
                            "Visit http://marmotta.apache.org for further details");
                }
            });
            popup.add(about);

            MenuItem issues = createMenuItem("Issues Reports", "https://issues.apache.org/jira/browse/MARMOTTA");
            popup.add(issues);

            MenuItem homepage = createMenuItem("Project Homepage", "http://marmotta.apache.org");
            popup.add(homepage);


            // load icon image
            try {
                Image image = ImageIO.read(SystrayListener.class.getResource("systray.png"));
                icon = new TrayIcon(image,"Apache Marmotta",popup);
                icon.setImageAutoSize(true);
                tray.add(icon);

            } catch (IOException e) {
                log.error("SYSTRAY: could not load the logo for system tray",e);
            } catch (AWTException e) {
                log.error("SYSTRAY: tray icon could not be added");
            }
        }
    }




    private MenuItem createMenuItem(final String label, final String uriString) {
        MenuItem entry = new MenuItem(label);
        if (Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            if(desktop.isSupported(Desktop.Action.BROWSE)) {
                ActionListener adminBrowserListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            URI uri = new URI(uriString);
                            desktop.browse(uri);

                        } catch (IOException e1) {
                            log.error("SYSTRAY: could not access system browser, access to "+label+" disabled");
                        } catch (URISyntaxException e1) {
                            log.error("SYSTRAY: could not build URI to administration service, access to "+label+" disabled");
                        }

                    }
                };
                entry.addActionListener(adminBrowserListener);

            } else {
                entry.setEnabled(false);
            }
        } else {
            entry.setEnabled(false);
        }
        return entry;
    }


    private void performServerShutdown() {
        try{
            MBeanServer server = (MBeanServer)Class.forName("org.apache.catalina.mbeans.MBeanUtils").getMethod("createServer").invoke(null);
            ObjectName name;
            if(isTomcat6()) {
                // Tomcat 6.x
                name = new ObjectName("Catalina:type=Service,serviceName=Catalina");
                server.invoke(name, "stop", new Object[0], new String[0]);
                log.warn("shutting down Apache Tomcat server on user request");
            } else if(isTomcat7()) {
                // Tomcat 7.x
                name = new ObjectName("Catalina", "type", "Service");
                server.invoke(name, "stop", new Object[0], new String[0]);
                log.warn("shutting down Apache Tomcat server on user request");
            }
        } catch (Exception ex) {
            log.error("shutting down other servers than Apache Tomcat is not supported",ex);
        }

        // ensure complete shutdown
        System.exit(0);

    }


    /**
     * Return true if Tomcat 6.x is detected; tests for presence of class org.apache.catalina.ServerFactory
     * @return
     */
    private boolean isTomcat6() {
        try {
            Class.forName("org.apache.catalina.ServerFactory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    /**
     * Return true if Tomcat 7.x is detected; tests for presence of class org.apache.catalina.CatalinaFactory
     * @return
     */
    private boolean isTomcat7() {
        try {
            Class.forName("org.apache.catalina.CatalinaFactory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


}
