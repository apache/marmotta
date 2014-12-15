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
package org.apache.marmotta.platform.security.model;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.apache.marmotta.platform.security.util.SubnetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SecurityConstraint implements Comparable<SecurityConstraint> {

    public enum Type { PERMISSION, RESTRICTION };


    private static Logger log = LoggerFactory.getLogger(SecurityConstraint.class);


    private Type type;


    private String name;

    /**
     * The URL pattern as a regular expression starting from the application root
     */
    private String urlPattern;

    /**
     * The host patterns as a list of strings in CIDR notation (e.g. 127.0.0.1/24, 192.168.100.2/32)
     */
    private Set<SubnetInfo> hostPatterns;


    private boolean enabled = true;


    /**
     * The names of the roles for which access is granted
     */
    private Set<String> roles;

    /**
     * The methods to which this security constraint applies
     */
    private Set<HTTPMethods> methods;

    /**
     * The priority of this security constraint
     */
    private int priority = 1;


    public SecurityConstraint(Type type, String name, String urlPattern, boolean enabled) {
        this.type = type;
        this.name = name;
        this.enabled = enabled;
        this.urlPattern = urlPattern;

        roles   = new HashSet<String>();
        methods = new HashSet<HTTPMethods>();
        hostPatterns = new HashSet<SubnetInfo>();
    }

    public SecurityConstraint(Type type, String name, String urlPattern, boolean enabled, int priority) {
        this(type, name, urlPattern, enabled);
        this.priority = priority;
    }

    /**
     * Check whether the security constraint matches with the servlet request. Applies method, URL and
     * remote address matching
     *
     * @param request
     * @return
     */
    public boolean matches(HttpServletRequest request) {
        return enabled && matchesMethod(request) && matchesAddress(request) && matchesUrl(request) && matchesRoles(request);
    }


    private boolean matchesMethod(HttpServletRequest request) {
        // match any method
        if(methods.size() == 0) {
            return true;
        }

        HTTPMethods method = HTTPMethods.parse(request.getMethod());

        if(method != null) {
            return methods.contains(method);
        } else {
            // no method given
            log.warn("request did not contain a supported HTTP method");
            return false;
        }
    }

    private boolean matchesUrl(HttpServletRequest request) {
        try {
            URL url = new URL(request.getRequestURL().toString());
            String prefix = request.getContextPath();
            String path = null;
            if(url.getPath().startsWith(prefix)) {
                path = url.getPath().substring(prefix.length());

                return path.matches(urlPattern);

            } else {
                return false;
            }



        } catch(MalformedURLException ex) {
            log.error("the request URL {} was invalid",request.getRequestURL().toString());
            return false;
        }


    }

    /**
     * Check whether the remote address of the request matches one of the host patterns (CIDR)
     * @param request
     * @return true if there are no host patterns configured or one of the host patterns matches the remote address
     */
    private boolean matchesAddress(HttpServletRequest request) {
        if(hostPatterns.size() == 0) {
            return true;
        }


        for(SubnetInfo hostPattern : hostPatterns) {
            if(hostPattern.getHostAddress().equals(request.getRemoteAddr()) || hostPattern.isInRange(request.getRemoteAddr())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the request user is in one of the specified roles.
     * @param request
     * @return true if no roles are set or the user is in one of the configured roles
     */
    private boolean matchesRoles(HttpServletRequest request) {
        if(roles.size() == 0) {
            return true;
        } else {
            Set<String> userRoles = (Set<String>)request.getAttribute("user.roles");
            if(userRoles != null) {
                for(String role : roles) {
                    if(userRoles.contains(role)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(SecurityConstraint o) {
        if(this.priority > o.priority) {
            return -1;
        } else if(o.priority > this.priority) {
            return 1;
        } else {
            return 0;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public Set<SubnetInfo> getHostPatterns() {
        return hostPatterns;
    }

    public void setHostPatterns(Set<SubnetInfo> hostPattern) {
        this.hostPatterns = hostPattern;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<HTTPMethods> getMethods() {
        return methods;
    }

    public void setMethods(Set<HTTPMethods> methods) {
        this.methods = methods;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("security constraint ").append(name).append(":");
        if(type == Type.PERMISSION) {
            s.append(" allow ");
        } else {
            s.append(" deny ");
        }
        if(methods.size() > 0) {
            s.append("{");
            Joiner.on(',').appendTo(s, methods);
            s.append("} of ");
        }
        s.append(urlPattern);
        if(hostPatterns.size() > 0) {
            s.append(" from ");
            Joiner.on(',').appendTo(s, Iterables.transform(hostPatterns,new Function<SubnetInfo, String>() {
                @Override
                public String apply(SubnetInfo subnetInfo) {
                    return subnetInfo.getCidrSignature();
                }
            }));
        }
        s.append(": ");
        if(enabled) {
            s.append(" enabled");
            if(roles.size() > 0) {
                s.append(" to ");
                Joiner.on(", ").appendTo(s, roles);
            }
        } else {
            s.append(" unrestricted");
        }
        return s.toString();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
