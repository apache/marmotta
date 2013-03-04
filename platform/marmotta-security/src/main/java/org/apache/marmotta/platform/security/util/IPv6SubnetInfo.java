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

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Implements IPv6 Subnets using BigInteger to perform subnet matching.
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public class IPv6SubnetInfo extends SubnetInfo {

    // full 128 bit representation
    private static BigInteger allbits   = new BigInteger("ffffffffffffffffffffffffffffffff",16);

    private BigInteger networkNetmask;

    private Inet6Address hostAddress;

    private Inet6Address networkAddress;
    
    private BigInteger networkBitmap;
    
    private int prefixLength;

    public IPv6SubnetInfo(Inet6Address address, int prefixLength) throws UnknownHostException {
        this.hostAddress    = address;
        this.networkNetmask = allbits.shiftRight(128-prefixLength).shiftLeft(128-prefixLength);
        this.prefixLength   = prefixLength;

        BigInteger inetaddr =  new BigInteger(address.getAddress());

        byte[] network_bytes = new byte[16];
        byte[] intbytes = inetaddr.and(networkNetmask).toByteArray();
        for(int i=0; i<16 && intbytes.length - i >0; i++) {
            network_bytes[16-i-1] = intbytes[intbytes.length-i-1];
        }

        this.networkAddress  = (Inet6Address) InetAddress.getByAddress(network_bytes);
        this.networkBitmap   = new BigInteger(network_bytes);

    }

    /**
     * Return the string representation of the IP hostAddress represented by this subnet.
     *
     * @return
     */
    @Override
    public String getHostAddress() {
        return hostAddress.getHostAddress();
    }

    /**
     * Return the string representation of the IP hostAddress represented by the subnet with the networkNetmask applied.
     *
     * @return
     */
    @Override
    public String getNetworkAddress() {
        return networkAddress.getHostAddress();
    }

    /**
     * Return the CIDR representation of this subnet, depending on whether it is an IPv4 or an IPv6 subnet.
     *
     * @return
     */
    @Override
    public String getCidrSignature() {
        return networkAddress.getHostAddress()+"/"+prefixLength;
    }

    /**
     * Return true if the IP hostAddress string representation passed as argument is in the range
     * of the subnet represented by this SubnetInfo.
     *
     * @param address
     * @return
     */
    @Override
    public boolean isInRange(String address) {

        try {
            InetAddress otherAddress = InetAddress.getByName(address);
            
            if(otherAddress instanceof Inet6Address) {
                BigInteger otherAddressInt = new BigInteger(otherAddress.getAddress());

                // the other hostAddress is inside the network if we can first "and" it with the networkNetmask and
                // then an xor with the network hostAddress yields 0
                BigInteger maskMatch = otherAddressInt.and(networkNetmask).xor(networkBitmap);
                return maskMatch.equals(BigInteger.ZERO);
            }
            
        } catch (UnknownHostException e) { }

        return false;
    }
}
