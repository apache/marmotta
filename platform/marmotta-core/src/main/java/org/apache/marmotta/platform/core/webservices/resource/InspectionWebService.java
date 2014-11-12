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
package org.apache.marmotta.platform.core.webservices.resource;

import com.google.common.net.HttpHeaders;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;
import static org.apache.marmotta.commons.sesame.repository.ResourceUtils.*;

@Path("/" + ConfigurationService.INSPECT_PATH)
public class InspectionWebService {

    private static final String  UUID_PATTERN = "/{uuid:[^#?]+}";
    private static final String  CHARSET      = "utf-8";
    private static final String  DEFAULT_BATCH_SIZE = "20";

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    @GET
    @Path("/subject")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public List<TriplePoJo> listSubjectUsage(@QueryParam("uri") String uri, @QueryParam("start") @DefaultValue("0") long offset,
            @QueryParam("limit") @DefaultValue(DEFAULT_BATCH_SIZE) int batchSize) {

        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                URI r = getUriResource(conn, uri);
                if (r != null) return
                        buildResultList(conn, r, null, null, null, offset, batchSize);
                else
                    return Collections.emptyList();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex,InspectionWebService.class);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("/predicate")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public List<TriplePoJo> listPredicatetUsage(@QueryParam("uri") String uri, @QueryParam("start") @DefaultValue("0") long offset,
            @QueryParam("limit") @DefaultValue(DEFAULT_BATCH_SIZE) int batchSize) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                URI r = getUriResource(conn, uri);
                if (r != null)
                    return buildResultList(conn, null, r, null, null, offset, batchSize);
                else
                    return Collections.emptyList();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex,InspectionWebService.class);
            return Collections.emptyList();
        }
    }


    @GET
    @Path("/object")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public List<TriplePoJo> listObjectUsage(@QueryParam("uri") String uri, @QueryParam("start") @DefaultValue("0") long offset,
            @QueryParam("limit") @DefaultValue(DEFAULT_BATCH_SIZE) int batchSize) {

        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                URI r = getUriResource(conn, uri);
                if (r != null)
                    return buildResultList(conn, null, null, r, null, offset, batchSize);
                else
                    return Collections.emptyList();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex,InspectionWebService.class);
            return Collections.emptyList();
        }
    }

    @GET
    @Path("/context")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public List<TriplePoJo> listContextUsage(@QueryParam("uri") String uri, @QueryParam("start") @DefaultValue("0") long offset,
            @QueryParam("limit") @DefaultValue(DEFAULT_BATCH_SIZE) int batchSize) {

        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                URI r = getUriResource(conn, uri);
                if (r != null)
                    return buildResultList(conn, null, null, null, r, offset, batchSize);
                else
                    return Collections.emptyList();
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex,InspectionWebService.class);
            return Collections.emptyList();
        }
    }

    private List<TriplePoJo> buildResultList(RepositoryConnection conn, URI s, URI p, URI o, URI c, long start, int limit) throws RepositoryException {
        List<TriplePoJo> result = new ArrayList<InspectionWebService.TriplePoJo>();
        RepositoryResult<Statement> triples = c != null ? conn.getStatements(s,p,o,true,c) : conn.getStatements(s,p,o,true);
        // skip until start
        for(int i = 0; i<start && triples.hasNext(); i++) {
            triples.next();
        }
        // retrieve until limit
        for(int i=0; i<limit && triples.hasNext(); i++) {
            result.add(new TriplePoJo(triples.next()));
        }
        triples.close();
        return result;
    }

    protected static class TriplePoJo {
        private final long id;
        private final String s, p, o, c;

        public TriplePoJo(Statement t) {
            id = ResourceUtils.getId(t);
            s = t.getSubject().toString();
            p = t.getPredicate().toString();
            o = t.getObject().toString();
            c = t.getContext() != null ? t.getContext().toString() : "";
        }

        public long getId() {
            return id;
        }

        public String getS() {
            return s;
        }

        public String getP() {
            return p;
        }

        public String getO() {
            return o;
        }

        public String getC() {
            return c;
        }
    }

    @GET
    @Path(UUID_PATTERN)
    @Produces("text/html")
    public Response inspectLocalResource(@PathParam("uuid") String uuid, @QueryParam("sOffset") @DefaultValue("0") long sOffset,
            @QueryParam("pOffset") @DefaultValue("0") long pOffset, @QueryParam("oOffset") @DefaultValue("0") long oOffset,
            @QueryParam("cOffset") @DefaultValue("0") long cOffset, @QueryParam("limit") @DefaultValue(DEFAULT_BATCH_SIZE) int batchSize)
                    throws UnsupportedEncodingException {
        String uri = configurationService.getBaseUri() + ConfigurationService.RESOURCE_PATH + "/" + uuid;
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                Resource rsc = getUriResource(conn, uri);
                if (rsc == null) {
                    rsc = getAnonResource(conn, uuid);
                }
                if (rsc == null)
                    return Response.status(Status.NOT_FOUND).entity("Not found: " + uuid).build();
                else
                    return inspectResource(conn, rsc, sOffset, pOffset, oOffset, cOffset, batchSize);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex,InspectionWebService.class);
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/")
    @Produces("text/html")
    public Response inspectRemoteResource(@QueryParam("uri") String uri, @QueryParam("sOffset") @DefaultValue("0") long sOffset,
            @QueryParam("pOffset") @DefaultValue("0") long pOffset, @QueryParam("oOffset") @DefaultValue("0") long oOffset,
            @QueryParam("cOffset") @DefaultValue("0") long cOffset, @QueryParam("limit") @DefaultValue(DEFAULT_BATCH_SIZE) int batchSize)
                    throws UnsupportedEncodingException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                Resource rsc = getUriResource(conn, uri);
                if (rsc == null)
                    return Response.status(Status.NOT_FOUND).entity("Not found: " + uri).build();
                else
                    return inspectResource(conn, rsc, sOffset, pOffset, oOffset, cOffset, batchSize);
            } finally {
                conn.commit();
                conn.close();
            }
        } catch(RepositoryException ex) {
            handleRepositoryException(ex,InspectionWebService.class);
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    private Response inspectResource(RepositoryConnection conn, Resource rsc, long subjOffset, long propOffset, long objOffset, long ctxOffset, int limit)
            throws UnsupportedEncodingException, RepositoryException {
        if (rsc == null)
            return Response.status(Status.NOT_FOUND).entity("Not found").build();
        URI uri = null;
        if (rsc instanceof URI) {
            uri = (URI) rsc;
        }
        // Well, this is rather hacky...
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);

        ps.println("<!DOCTYPE HTML><html><head>");

        ps.printf("<title>Inspect %s</title>%n", rsc.stringValue());
        ps.println("<style type='text/css'>");
        ps.println("table {border: 2px solid #006d8f;width: 100%;border-collapse: collapse;}"
                + "html, body{font-family: sans-serif;}"
                + "table th {background-color: #006d8f;color: white;}"
                + "table tr {}"
                + "table tr.even {background-color: #dff7ff;}"
                + "table tr.odd {background-color: lightBlue;}"
                + "table th, table td {padding: 2px;}"
                + "table tr:hover {background-color: white;}"
                + ".isDeleted { color: red; font-style: italic; }"
                + ".deleted {width: 20px; height: 20px; display: inline-block; border-radius: 10px; float: right}"
                + ".deleted.true {background-color: red;}"
                + ".deleted.false {background-color: darkGreen;}"
                + ".reasoned a { border-radius: 10px; display: inline-block; float: right; background-color: orange; width: 20px; height: 20px; color: white; text-decoration: none; margin-right: 5px; text-align: center; font-family: serif; font-weight: bolder;}"
                + ".info-dialog { border:1px solid black; width:50%; position:absolute; top:100px; left:25%; background-color:white; z-index:2; padding-top:10px; min-height:100px; overflow:auto; display:none;}"
                + ".info-dialog button.close-button { position:absolute; top:5px; right:5px; } "
                + ".info-dialog iframe { border:none; width:100%; }");

        ps.println("</style>");

        ps.println();
        ps.println("</head><body>");

        ps.printf("<h1>Inspect %s</h1>%n<div>ShortName: %s<span class='%s'>%<s</span></div>%n", rsc.stringValue(), getLabel(conn,rsc), "");
        Date created = ResourceUtils.getCreated(conn, rsc);
        if(created != null) {
            ps.printf("<div>Created: <span>%tF %<tT</span> by <span>%s</span></div>%n", created, createInspectLink(conn, null, null, ""));
        }
        ps.printf("<div>Last Modified: <span>%tF %<tT</span></div>%n", ResourceUtils.getLastModified(conn, rsc));

        // Outgoing
        printOutgoing(conn, rsc, ps, subjOffset, limit);

        // Incoming
        printIncoming(conn, rsc, ps, objOffset, limit);

        // Links
        printLinks(conn, uri, ps, propOffset, limit);

        // Context
        printContext(conn, uri, ps, ctxOffset, limit);

        ps.println();
        ps.println("</body></html>");
        return Response.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/html;charset=" + CHARSET)
                .header(HttpHeaders.LAST_MODIFIED, ResourceUtils.getLastModified(conn, rsc))
                .entity(os.toString(CHARSET)).build();
    }

    protected void printLinks(RepositoryConnection conn, URI rsc, PrintStream ps, long offset, int limit) throws UnsupportedEncodingException, RepositoryException {
        if (rsc == null) return;
        int i = 0;
        RepositoryResult<Statement> ts = conn.getStatements(null, rsc, null, true);

        // skip until start
        for(int j = 0; j<offset && ts.hasNext(); j++) {
            ts.next();
        }
        if (ts.hasNext()) {
            Set<Statement> triples = new HashSet<Statement>();
            for(int j = 0; j<limit && ts.hasNext(); j++) {
                triples.add(ts.next());
            }

            ps.printf("<h2>Connections</h2>%n");
            ps.printf("<span class='count'>Showing %d links:</span> %s%n", triples.size(), createInspectLink(conn, rsc, "next", "&pOffset=" + (offset + limit)));

            ps.printf("<table>%n  <thead><tr><th>Source</th><th>Target</th><th>Context</th><th></th><th></th></tr></thead>%n  <tbody>%n");
            for (Statement t : triples) {
                ps.printf("    <tr class='%s'><td>%s</td><td>%s</td><td>%s</td><td><span class='reasoned'>%s</span></td><td><span class='deleted %b'></span></td></tr>%n",
                        i++ % 2 == 0 ? "even" : "odd", createInspectLink(conn, t.getSubject()), createInspectLink(conn,t.getObject()), createInspectLink(conn,t.getContext()),
                                ResourceUtils.isInferred(conn,t) ? createInfo(ResourceUtils.getId(t)) : "", ResourceUtils.isDeleted(conn,t));
            }
            ps.printf("  </tbody>%n</table>");
        } else if (offset > 0) {
            ps.printf("<h2>Connections</h2>%n");
            ps.printf("<span class='count'>Less than %d links, reset offset</span>%n", offset);
        }
        ts.close();
    }

    protected void printIncoming(RepositoryConnection conn, Resource rsc, PrintStream ps, long offset, int limit) throws UnsupportedEncodingException, RepositoryException {
        int i = 0;
        RepositoryResult<Statement> ts = conn.getStatements(null, null, rsc, true);

        // skip until start
        for(int j = 0; j<offset && ts.hasNext(); j++) {
            ts.next();
        }
        if (ts.hasNext()) {
            Set<Statement> triples = new HashSet<Statement>();
            for(int j = 0; j<limit && ts.hasNext(); j++) {
                triples.add(ts.next());
            }

            ps.printf("<h2>Incoming Links</h2>%n");
            ps.printf("<span class='count'>Showing %d incoming links:</span> %s%n", triples.size(), createInspectLink(conn,rsc, "next", "&oOffset=" + (offset + limit)));
            ps.printf("<table>%n  <thead><tr><th>Source</th><th>Link</th><th>Context</th><th></th><th></th></tr></thead>%n  <tbody>%n");
            for (Statement t : triples) {
                ps.printf(
                        "    <tr class='%s'><td>%s</td><td>%s</td><td>%s</td><td><span class='reasoned'>%s</span></td><td><span class='deleted %b'></span></td></tr>%n",
                        i++ % 2 == 0 ? "even" : "odd", createInspectLink(conn, t.getSubject()), createInspectLink(conn,t.getObject()), createInspectLink(conn,t.getContext()),
                        ResourceUtils.isInferred(conn,t) ? createInfo(ResourceUtils.getId(t)) : "", ResourceUtils.isDeleted(conn,t));
            }
            ps.printf("  </tbody>%n</table>");
        } else if (offset > 0) {
            ps.printf("<h2>Incoming Links</h2>%n");
            ps.printf("<span class='count'>No more incoming links at offset %d</span>%n", offset);
        }
        ts.close();
    }

    protected void printOutgoing(RepositoryConnection conn, Resource rsc, PrintStream ps, long offset, int limit) throws UnsupportedEncodingException, RepositoryException {
        int i = 0;
        RepositoryResult<Statement> ts = conn.getStatements(rsc, null, null, true);

        // skip until start
        for(int j = 0; j<offset && ts.hasNext(); j++) {
            ts.next();
        }
        if (ts.hasNext()) {
            Set<Statement> triples = new HashSet<Statement>();
            for(int j = 0; j<limit && ts.hasNext(); j++) {
                triples.add(ts.next());
            }
            ps.printf("<h2>Outgoing Links</h2>%n");
            ps.printf("<span class='count'>Showing %d outgoing links:</span> %s%n", triples.size(),
                    createInspectLink(conn, rsc, "next", "&sOffset=" + (offset + limit)));
            ps.printf("<table>%n  <thead><tr><th>Link</th><th>Target</th><th>Context</th><th></th><th></th></tr></thead>%n  <tbody>%n");
            for (Statement t : triples) {
                ps.printf(
                        "    <tr class='%s'><td>%s</td><td>%s</td><td>%s</td><td><span class='reasoned'>%s</span></td><td><span class='deleted %b'></span></td></tr>%n",
                        i++ % 2 == 0 ? "even" : "odd", createInspectLink(conn, t.getSubject()), createInspectLink(conn,t.getObject()), createInspectLink(conn,t.getContext()),
                        ResourceUtils.isInferred(conn,t) ? createInfo(ResourceUtils.getId(t)) : "", ResourceUtils.isDeleted(conn,t));
            }
            ps.printf("  </tbody>%n</table>");
        } else if (offset > 0) {
            ps.printf("<h2>Outgoing Links</h2>%n");
            ps.printf("<span class='count'>No more outgoing links at offset %d</span>%n", offset);
        }
        ts.close();
    }

    protected void printContext(RepositoryConnection conn, Resource rsc, PrintStream ps, long offset, int limit) throws UnsupportedEncodingException, RepositoryException {
        if (rsc == null) return;
        int i = 0;
        RepositoryResult<Statement> ts = conn.getStatements(null, null, null, true, rsc);

        // skip until start
        for(int j = 0; j<offset && ts.hasNext(); j++) {
            ts.next();
        }
        if (ts.hasNext()) {
            Set<Statement> triples = new HashSet<Statement>();
            for(int j = 0; j<limit && ts.hasNext(); j++) {
                triples.add(ts.next());
            }
            ps.printf("<h2>Context</h2>%n");
            ps.printf("<span class='count'>Showing %d triples in Context:</span> %s%n", triples.size(),
                    createInspectLink(conn, rsc, "next", "&cOffset=" + (offset + limit)));
            ps.printf("<table>%n  <thead><tr><th>Subject</th><th>Property</th><th>Object</th><th></th><th></th></tr></thead>%n  <tbody>%n");
            for (Statement t : triples) {
                ps.printf(
                        "    <tr class='%s'><td>%s</td><td>%s</td><td>%s</td><td><span class='reasoned'>%s</span></td><td><span class='deleted %b'></span></td></tr>%n",
                        i++ % 2 == 0 ? "even" : "odd", createInspectLink(conn, t.getSubject()), createInspectLink(conn,t.getObject()), createInspectLink(conn,t.getContext()),
                        ResourceUtils.isInferred(conn,t) ? createInfo(ResourceUtils.getId(t)) : "", ResourceUtils.isDeleted(conn,t));
            }
            ps.printf("  </tbody>%n</table>");
        } else if (offset > 0) {
            ps.printf("<h2>Context</h2>%n");
            ps.printf("<span class='count'>Less than %d triples in context, reset offset.</span>%n", offset);
        }

    }

    private String createInspectLink(RepositoryConnection conn, Value node) throws UnsupportedEncodingException {
        return createInspectLink(conn, node, null, "");
    }

    private String createInspectLink(RepositoryConnection conn, Value node, String linkText, String extraQS) throws UnsupportedEncodingException {
        if(node == null) return "undefined";
        if (extraQS == null) {
            extraQS = "";
        }
        if (node instanceof Literal) {
            Literal lit = (Literal) node;
            if (linkText == null) {
                linkText = lit.getDatatype().stringValue();
            }
            StringBuilder sb = new StringBuilder("\"" + lit.getLabel().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                    + "\"");
            if (lit.getLanguage() != null) {
                sb.append("@").append(lit.getLanguage());
            } else if (lit.getDatatype() != null) {
                sb.append("^^").append(
                        String.format("<a href='%s?uri=%s%s'>%s</a>", configurationService.getServerUri() + "inspect",
                                URLEncoder.encode(lit.getDatatype().stringValue(), CHARSET), extraQS, linkText));
            }
            return sb.toString();
        }
        if (node instanceof BNode) {
            BNode rsc = (BNode) node;
            if (linkText == null) {
                linkText = rsc.toString();
            }
            return String.format("<a href='%s/%s%s'>%s</a>", configurationService.getServerUri() + "inspect", rsc.getID(),
                    extraQS.replaceFirst("^&", "?"), linkText);
        }
        if (node instanceof URI) {
            URI rsc = (URI) node;
            if (linkText == null) {
                linkText = rsc.toString();
            }
            return String.format("<a href='%s?uri=%s%s'>%s</a>", configurationService.getServerUri() + "inspect",
                    URLEncoder.encode(rsc.toString(), CHARSET), extraQS, linkText);
        }
        return "UNKNOWN";
    }

//    private String buildResourceLink(RepositoryConnection conn, URI resource, String rel, String mime) {
//        final String src = configurationService.getServerUri(), base = configurationService.getBaseUri();
//
//        if (src.equals(base) && resource.toString().startsWith(base + ConfigurationService.RESOURCE_PATH + "/")) {
//            final String uuid;
//            uuid = resource.toString().substring((base + ConfigurationService.RESOURCE_PATH + "/").length());
//            return String.format("%s%s/%s/%s", base, rel, mime, uuid);
//        } else {
//            try {
//                return String.format("%s%s/%s?uri=%s", src, rel, mime, URLEncoder.encode(resource.toString(), CHARSET));
//            } catch (UnsupportedEncodingException e) {
//                return String.format("%s%s/%s?uri=%s", src, rel, mime, resource.toString());
//            }
//        }
//    }

    private String createInfo(long id) {
        StringBuilder b = new StringBuilder();
        String closer = "<button onclick='document.getElementById(\"info" + id + "\").style.display = \"none\"' class='close-button'>X</button>";
        String iframe = "<iframe id='iframe" + id + "' src='' style=''></iframe>";
        b.append("<a href='#' title='justify this triple' onclick='document.getElementById(\"iframe").append(id).append("\").src=\"").append(configurationService.getServerUri()).append("core/public/html/reasoning.html#").append(id).append("\";document.getElementById(\"info").append(id).append("\").style.display = \"block\";'>i</a>");
        b.append("<div id='info").append(id).append("' class='info-dialog'>");
        b.append(iframe).append(closer);
        b.append("</div>");
        return b.toString();
    }


}
