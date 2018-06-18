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
package org.apache.marmotta.platform.security.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Subnet information about an IPv4 or IPv6 subnet. Specific implementations for IPv4 and IPv6 provided by
 * the LMF.
 * 
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class SubnetInfo {

    /**
     * Return the string representation of the IP address used to initialise this subnet.
     * 
     * @return
     */
    public abstract String getHostAddress();


    /**
     * Return the string representation of the IP address represented by the subnet with the netmask applied.
     * @return
     */
    public abstract String getNetworkAddress();
    
    
    /**
     * Return true if the IP address string representation passed as argument is in the range
     * of the subnet represented by this SubnetInfo.
     *
     * @param address
     * @return
     */
    public abstract boolean isInRange(String address);


    /**
     * Return the CIDR representation of this subnet, depending on whether it is an IPv4 or an IPv6 subnet.
     *
     * @return
     */
    public abstract String getCidrSignature();
    
    
    public static SubnetInfo getSubnetInfo(InetAddress address, int netmask) throws UnknownHostException {
        if(address instanceof Inet6Address) {
            return new IPv6SubnetInfo((Inet6Address) address,netmask);
        } else {
            return new IPv4SubnetInfo((Inet4Address) address,netmask);
        }
    }

    public static SubnetInfo getSubnetInfo(InetAddress address) throws UnknownHostException {
        if(address instanceof Inet6Address) {
            return getSubnetInfo(address,128);
        } else {
            return getSubnetInfo(address,32);
        }
    }
    
    
    public static SubnetInfo getSubnetInfo(String cidrAddress) throws UnknownHostException {
        String[] components = cidrAddress.split("/");
        
        InetAddress address = InetAddress.getByName(components[0]);
        int netmask = components.length > 1 ? Integer.parseInt(components[1]) : (address instanceof Inet4Address?32:128);
        return getSubnetInfo(address,netmask);
    }
}
