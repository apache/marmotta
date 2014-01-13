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
package org.apache.marmotta.splash.startup;

import static org.apache.marmotta.splash.common.MarmottaStartupHelper.checkServerName;
import static org.apache.marmotta.splash.common.MarmottaStartupHelper.getServerPort;
import static org.apache.marmotta.splash.common.MarmottaStartupHelper.getStartupProperties;
import static org.apache.marmotta.splash.common.MarmottaStartupHelper.listHostAddresses;
import static org.apache.marmotta.splash.common.MarmottaStartupHelper.storeStartupProperties;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.marmotta.splash.common.ui.MessageDialog;
import org.apache.marmotta.splash.common.ui.SelectionDialog;
import org.apache.marmotta.splash.common.ui.SelectionDialog.Option;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class StartupListener implements LifecycleListener {

    protected static Log log = LogFactory.getLog(StartupListener.class);

    public StartupListener() {
        super();
    }

    /**
     * React on the AFTER_START_EVENT of Tomcat and startup the browser to point to the Marmotta installation. Depending
     * on the state of the Marmotta installation, the following actions are carried out:
     * <ul>
     *     <li>in case the Marmotta is started for the first time, show a dialog box with options to select which IP-address to use for
     *     configuring the Marmotta; the IP address will be stored in a separate properties file in MARMOTTA_HOME</li>
     *     <li>in case the Marmotta has already been configured but the IP address that was used is no longer existing on the server,
     *     show a warning dialog (this can happen e.g. for laptops with dynamically changing network configurations)</li>
     *     <li>otherwise, open a browser using the network address that was used previously</li>
     * </ul>
     *
     * @param event LifecycleEvent that has occurred
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        if(event.getType().equals(Lifecycle.AFTER_START_EVENT)) {
            if(!GraphicsEnvironment.isHeadless()) {

                String serverName = null;
                int serverPort = 0;

                // open browser window
                if (Desktop.isDesktopSupported()) {

                    Properties startupProperties = getStartupProperties();
                    if(startupProperties.getProperty("startup.host") != null && startupProperties.getProperty("startup.port") != null) {

                        serverName = startupProperties.getProperty("startup.host");
                        serverPort = Integer.parseInt(startupProperties.getProperty("startup.port"));


                        if(!checkServerName(serverName) || serverPort != getServerPort()) {
                            MessageDialog.show("Warning",
                                    "Configured server name not found",
                                    "The host name ("+serverName+") that has been used to configure this \n" +
                                    "installation is no longer available on this server. The system \n" +
                                    "might behave unexpectedly. Please consider using a localhost configuration \n" +
                                    "for systems with dynamic IP addresses!");
                        }


                    } else {
                        // show a dialog listing all available addresses of this server and allowing the user to
                        // chose
                        List<Option> choices = new ArrayList<>();

                        Map<String,List<InetAddress>> addressList = listHostAddresses();

                        List<String> hostNames = new ArrayList<String>(addressList.keySet());
                        Collections.sort(hostNames);

                        int loopback = -1;
                        for (int i = 0; i < hostNames.size(); i++) {
                            String hostName = hostNames.get(i);
                            List<InetAddress> addresses = addressList.get(hostName);
                            String label = hostName + " \n(";
                            for(Iterator<InetAddress> it = addresses.iterator(); it.hasNext(); ) {
                                label += it.next().getHostAddress();
                                if(it.hasNext()) {
                                    label +=", ";
                                }
                            }
                            label += ")";

                            String text;

                            if(addresses.get(0).isLoopbackAddress()) {
                                text = "Local IP-Address. Recommended for Laptop use or Demonstration purposes";
                                loopback = loopback<0?i:loopback;
                            } else {
                                text = "Public IP-Address. Recommended for Workstation or Server use";
                            }

                            choices.add(new Option(label, text));
                        }

                        int choice = SelectionDialog.select("Select Server Address",
                                "Select host address to use for configuring the\nApache Marmotta Platform.",
                                WordUtils.wrap("For demonstration purposes or laptop installations it is recommended to select \""+(loopback<0?"localhost":hostNames.get(loopback))+"\" below. For server and workstation installations, please select a public IP address.", 60), 
                                choices, loopback);

                        
                        if (choice < 0) {
                            log.error("No Server Address selected, server will shut down.");
                            throw new IllegalArgumentException("No Server Addess was selected");
                        }

                        serverName = hostNames.get(choice);
                        serverPort = getServerPort();

                        startupProperties.setProperty("startup.host",serverName);
                        startupProperties.setProperty("startup.port",serverPort+"");

                        storeStartupProperties(startupProperties);
                    }


                    final Desktop desktop = Desktop.getDesktop();
                    if(desktop.isSupported(Desktop.Action.BROWSE) && serverName != null && serverPort > 0) {
                        try {


                            URI uri = new URI("http",null,serverName,serverPort,"/",null,null);
                            desktop.browse(uri);

                        } catch (Exception e1) {
                            System.err.println("could not open browser window, message was: "+e1.getMessage());
                        }

                    }
                }
            }

        }
    }

}
