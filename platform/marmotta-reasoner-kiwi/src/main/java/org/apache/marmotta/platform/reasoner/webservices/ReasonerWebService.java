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
package org.apache.marmotta.platform.reasoner.webservices;

import info.aduna.iteration.Iterations;
import org.apache.marmotta.kiwi.model.rdf.KiWiLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.reasoner.model.program.Justification;
import org.apache.marmotta.kiwi.reasoner.model.program.Rule;
import org.apache.marmotta.platform.reasoner.services.ReasoningSailProvider;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
@SuppressWarnings("JpaQueryApiInspection")
@ApplicationScoped
@Path("/reasoner/engine")
@Produces({"application/json"})
public class ReasonerWebService {

    @Inject
    private Logger log;

    @Inject
    private ReasoningSailProvider provider;

    @POST
    @Path("/run")
    @Produces("text/plain")
    public Response rerunPrograms() throws Exception {
        log.debug("rerunning all reasoning programs");
        provider.reRunPrograms();

        return Response.ok("(re-)reasoning triggered").build();
    }


    @GET
    @Path("/justify")
    public Response justifyTriple(@QueryParam("id") Long tripleId) {
        if(tripleId != null) {
            try {
                StringBuilder result = new StringBuilder();
                List<Justification> justifications = Iterations.asList(provider.justify(tripleId));

                // group by triple
                HashMap<KiWiTriple,Set<Justification>> grouped = new HashMap<KiWiTriple, Set<Justification>>();
                for(Justification j : justifications) {
                    Set<Justification> tjs = grouped.get(j.getTriple());
                    if(tjs == null) {
                        tjs = new HashSet<Justification>();
                        grouped.put(j.getTriple(),tjs);
                    }
                    tjs.add(j);
                }
                result.append("[");

                for(Iterator<KiWiTriple> it = grouped.keySet().iterator(); it.hasNext(); ) {
                    KiWiTriple t = it.next();
                    result.append(formatJSON(t, grouped.get(t)));

                    if(it.hasNext()) {
                        result.append(",\n");
                    } else {
                        result.append("\n");
                    }
                }

                result.append("]");

                return Response.ok(result.toString()).build();
            } catch (SailException e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("no triple id given").build();
        }
    }


    private String formatJSON(KiWiTriple triple, Collection<Justification> justifications) {
        StringBuilder result = new StringBuilder();
        result.append("{\n ");

        result.append("\"triple\": " + formatJSON(triple) + ",\n");
        result.append("\"justifications\": [\n");
        for(Iterator<Justification> justs = justifications.iterator(); justs.hasNext(); ) {
            Justification justification = justs.next();
            result.append("  {\n");
            result.append("    \"triples\": [\n");
            for(Iterator<KiWiTriple> it = justification.getSupportingTriples().iterator(); it.hasNext(); ) {
                KiWiTriple t = it.next();
                result.append("      " + formatJSON(t));
                if(it.hasNext()) {
                    result.append(",\n");
                } else {
                    result.append("\n");
                }
            }
            result.append("    ],");
            result.append("    \"rules\": [\n");
            for(Iterator<Rule> it = justification.getSupportingRules().iterator(); it.hasNext(); ) {
                Rule r = it.next();
                result.append("      \""+r.toString().replace("\"","'")+"\"");
                if(it.hasNext()) {
                    result.append(",\n");
                } else {
                    result.append("\n");
                }
            }

            result.append("    ]");
            result.append("  }");
            if(justs.hasNext()) {
                result.append(",\n");
            } else {
                result.append("\n");
            }
        }
        result.append("]");
        result.append("}");

        return result.toString();
    }

    private String formatJSON(KiWiTriple triple) {
        return "{ " + formatJSON(triple.getSubject(),false) + " : { " + formatJSON(triple.getPredicate(), false) + " : [" + formatJSON(triple.getObject(), true) + "] } }";
    }

    private String formatJSON(KiWiNode node, boolean asObject) {
        if(node.isUriResource()) {
            String result =  "\""+ node.stringValue()+"\"";

            if(asObject) {
                return "{  \"type\" : \"uri\", \"value\" : "+result+" }";
            } else {
                return result;
            }
        } else if(node.isAnonymousResource()) {
            String result = "\"_:"+node.stringValue()+"\"";

            if(asObject) {
                return "{  \"type\" : \"bnode\", \"value\" : "+result+" }";
            } else {
                return result;
            }

        } else if(node.isLiteral()) {
            KiWiLiteral l = (KiWiLiteral)node;
            return "{ \"type\" : \"literal\"," +
                    " \"value\" : \"" + l.getContent().replace("\"","\\\"") + "\"" +
                    (l.getLocale() != null ? ", \"lang\" : \""+l.getLocale().getLanguage() + "\"" : "") +
                    (l.getType() != null ? ", \"type\" : \""+l.getType()+"\"" : "") +
                    " }";

        } else {
            return null;
        }
    }
}
