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
package org.apache.marmotta.splash.common;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Static utility methods used by several listeners.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class MarmottaStartupHelper {

    protected static Log log = LogFactory.getLog(MarmottaStartupHelper.class);



    public static final String getServerName() {
        Properties properties = getStartupProperties();
        return (String)properties.get("startup.host");
    }


    public static final int getServerPort() {
        try {
            MBeanServer mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
            ObjectName name = new ObjectName("Catalina", "type", "Server");
            Object server = mBeanServer.getAttribute(name, "managedResource");

            Object service = Array.get(server.getClass().getMethod("findServices").invoke(server), 0);
            Object connector = Array.get(service.getClass().getMethod("findConnectors").invoke(service),0);

            int port = (Integer)connector.getClass().getMethod("getPort").invoke(connector);
            return port;
        } catch (Exception ex) {
            log.error("could not determine server port, using 8080");
            return 8080;
        }
    }

    public static final Properties getStartupProperties() {
        Properties result = new Properties();

        final String home = System.getenv("MARMOTTA_HOME");

        if(home != null) {

            File dir_home = new File(home);
            dir_home.mkdirs();

            File startup_properties = new File(home + File.separator + "startup.properties");
            if(startup_properties.exists() && startup_properties.canRead()) {
                try {
                    FileReader reader = new FileReader(startup_properties);
                    result.load(reader);
                    reader.close();
                } catch (IOException e) {
                    log.error("I/O error while accessing startup properties file: "+e.getMessage());
                }
            }

        } else {
            log.error("MARMOTTA_HOME variable was not set; could not load/save properties");
        }
        return result;
    }

    public static final void storeStartupProperties(Properties properties) {
        final String home = System.getenv("MARMOTTA_HOME");

        if(home != null) {

            File dir_home = new File(home);
            dir_home.mkdirs();

            File startup_properties = new File(home + File.separator + "startup.properties");
            if(!startup_properties.exists() || startup_properties.canWrite()) {
                try {
                    Writer fileWriter = new FileWriter(startup_properties);
                    properties.store(fileWriter, "stored by marmotta startup; do not modify manually");
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    log.error("I/O error while accessing startup properties file: "+e.getMessage());
                }
            }

        } else {
            log.error("MARMOTTA_HOME variable was not set; could not load/save properties");
        }
    }



    public static final Map<String,List<InetAddress>> listHostAddresses() {
        Map<String,List<InetAddress>> result = new HashMap<String,List<InetAddress>>();
        try {
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while(ifs.hasMoreElements()) {
                NetworkInterface iface = ifs.nextElement();
                if(iface.isUp() && !iface.isPointToPoint()) {
                    Enumeration<InetAddress> addrs = iface.getInetAddresses();
                    while(addrs.hasMoreElements()) {
                        InetAddress addr = addrs.nextElement();

                        String hostName = addr.getHostName();

                        if(!hostName.equals(addr.getHostAddress())) {
                            // only take interfaces with a proper hostname configured

                            List<InetAddress> addresses = result.get(hostName);
                            if(addresses == null) {
                                addresses = new ArrayList<InetAddress>();
                                result.put(hostName,addresses);
                            }
                            addresses.add(addr);
                        }

                    }
                }
            }
            return result;
        } catch(SocketException ex) {
            log.warn("could not determine local IP addresses, will use localhost only");
            return Collections.emptyMap();
        }

    }

    /**
     * Check whether the servername represents a valid network interface of this sever.
     *
     * @param serverName
     * @return
     */
    public static final boolean checkServerName(String serverName) {
        try {
            InetAddress address = InetAddress.getByName(serverName);
            return NetworkInterface.getByInetAddress(address) != null;
        } catch (UnknownHostException e) {
            return false;
        } catch (SocketException e) {
            return false;
        }
    }

}
