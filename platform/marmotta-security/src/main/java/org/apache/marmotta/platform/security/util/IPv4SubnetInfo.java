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
package org.apache.marmotta.platform.security.util;

import org.apache.commons.net.util.SubnetUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Implementation of subnet information for IPv4, based on Apache Commons Net.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class IPv4SubnetInfo extends SubnetInfo {

    private SubnetUtils.SubnetInfo apacheInfo;

    public IPv4SubnetInfo(Inet4Address address, int netmask) {
        apacheInfo = new SubnetUtils(address.getHostAddress()+"/"+netmask).getInfo();
    }

    /**
     * Return the string representation of the IP address used to initialise this subnet.
     *
     * @return
     */
    @Override
    public String getHostAddress() {
        return apacheInfo.getAddress();
    }

    /**
     * Return the string representation of the IP address represented by the subnet with the netmask applied.
     *
     * @return
     */
    @Override
    public String getNetworkAddress() {
        return apacheInfo.getNetworkAddress();
    }

    /**
     * Return the CIDR representation of this subnet, depending on whether it is an IPv4 or an IPv6 subnet.
     *
     * @return
     */
    @Override
    public String getCidrSignature() {
        return apacheInfo.getCidrSignature();
    }

    /**
     * Return true if the IP address string representation passed as argument is in the range
     * of the subnet represented by this SubnetInfo.
     *
     * @param address
     * @return
     */
    @Override
    public boolean isInRange(String address) {
        try {
            if(InetAddress.getByName(address) instanceof Inet4Address) {
                return apacheInfo.isInRange(address);
            }
        } catch (UnknownHostException e) {}
        return false;
    }
}
