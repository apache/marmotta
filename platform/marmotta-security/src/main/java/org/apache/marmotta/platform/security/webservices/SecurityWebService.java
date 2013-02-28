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
package org.apache.marmotta.platform.security.webservices;

import org.apache.marmotta.platform.security.api.SecurityService;
import org.apache.marmotta.platform.security.model.HTTPMethods;
import org.apache.marmotta.platform.security.model.SecurityConstraint;
import org.apache.marmotta.platform.security.util.SubnetInfo;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A webservice providing access to the security constraints currently active in the system
 * 
 * @author Sebastian Schaffert
 */
@ApplicationScoped
@Path("/security")
public class SecurityWebService {

    @Inject
    private Logger log;

    @Inject
    private SecurityService securityService;

    @Path("/constraints")
    @GET
    @Produces("application/json")
    public Response listConstraints() {
        List<Map<String,Object>> result = Lists.transform(securityService.listSecurityConstraints(), new Function<SecurityConstraint, Map<String, Object>>() {
            @Override
            public Map<String, Object> apply(SecurityConstraint input) {
                return formatConstraint(input);
            }
        });

        return Response.ok().entity(result).build();

    }

    @Path("/constraints/{name}")
    @GET
    @Produces("application/json")
    public Response getConstraint(@PathParam("name") String name) {
        SecurityConstraint constraint = null;
        for(SecurityConstraint constraint1 : securityService.listSecurityConstraints()) {
            if(constraint1.getName().equals(name)) {
                constraint = constraint1;
                break;
            }
        }

        if (constraint != null)
            return Response.ok(formatConstraint(constraint)).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("constraint with name "+name+" does not exist").build();
    }

    private Map<String,Object> formatConstraint(SecurityConstraint constraint) {
        Map<String,Object> result = new HashMap<String, Object>();
        result.put("name",constraint.getName());
        result.put("pattern", constraint.getUrlPattern());
        result.put("type", constraint.getType().toString());

        List<String> methods = new ArrayList<String>();
        for(HTTPMethods method : constraint.getMethods()) {
            methods.add(method.toString());
        }
        result.put("methods",methods);

        List<String> hosts = new ArrayList<String>();
        for(SubnetInfo info : constraint.getHostPatterns()) {
            hosts.add(info.getCidrSignature());
        }
        result.put("hosts",hosts);

        result.put("roles",new ArrayList<String>(constraint.getRoles()));
        result.put("enabled", constraint.isEnabled() ? "true" : "false");
        result.put("priority", constraint.getPriority());

        return result;
    }

}
