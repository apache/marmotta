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
package org.apache.marmotta.platform.ldpath.webservices;

import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;
import static org.apache.marmotta.commons.sesame.repository.ResourceUtils.listOutgoing;
import static org.apache.marmotta.commons.sesame.repository.ResourceUtils.listResourcesByPrefix;
import static org.apache.marmotta.commons.sesame.repository.ResultUtils.iterable;

import org.apache.marmotta.platform.ldpath.api.LDPathService;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.apache.marmotta.platform.core.api.prefix.PrefixService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.services.prefix.PrefixCC;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.UriUtil;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.backend.sesame.SesameConnectionBackend;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/ldpath/util")
public class LDPathUtilWebService {

    private static final String FUNCTION_NAMESPACE = "function:ldpath#";

    private static final String FUNCTION_PREFIX = "fn";

    private static final String MODE_TRANSFORM = "transformer";

    private static final Pattern CURIE_PATTERN = Pattern.compile("(\\w+):(\\w*)");

    @Inject
    private SesameService sesameService;

    @Inject
    private LDPathService   ldPathService;

    @Inject
    private PrefixService   prefixService;

    @Inject
    private PrefixCC             prefixCC;

    @GET
    @Path("/namespaces")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public Map<String, String> listKnownNamespaces(@Context UriInfo info) {
        final PrefixService prefixService = createLocalPrefixService(info);
        Map<String, String> nss = new HashMap<String, String>();

        try {
            RepositoryConnection con = sesameService.getConnection();
            try {
                con.begin();
                for (Namespace ns : iterable(con.getNamespaces())) {
                    nss.put(ns.getPrefix(), ns.getName());
                }
                // commit added
                con.commit();
            } finally {
                con.close();
            }

        } catch (RepositoryException e) {
            handleRepositoryException(e,LDPathUtilWebService.class);
        }

        for (Map.Entry<String, String> e : prefixService.getMappings().entrySet()) {
            nss.put(e.getKey(), e.getValue());
        }
        nss.put(FUNCTION_PREFIX, FUNCTION_NAMESPACE);
        return nss;
    }

    @GET
    @Path("/prefix")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public Map<String, String> resolvePrefix(@QueryParam("prefix") String prefix, @Context UriInfo info) {
        final PrefixService prefixService = createLocalPrefixService(info);
        if (prefixService.containsPrefix(prefix))
            return Collections.singletonMap(prefix, prefixService.getNamespace(prefix));

        // As a fallback, try prefix.cc
        if (prefix != null) {
            final String namespace = prefixCC.getNamespace(prefix);
            if (namespace != null)
                return Collections.singletonMap(prefix, namespace);
        }
        return Collections.emptyMap();
    }

    @GET
    @Path("/complete")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public List<String> complete(@QueryParam("prefix") String prefix, @QueryParam("uri") String uri,
            @QueryParam("mode") @DefaultValue("path") String mode, @Context UriInfo info) {
        final int limit = 20;
        final PrefixService prefixService = createLocalPrefixService(info);
        if (uri != null) {
            // Complete <URI>
            final List<String> suggestions = new ArrayList<String>();
            for (String sug : getCompletions(uri, limit, mode)) {
                final String curie = prefixService.getCurie(sug);
                suggestions.add(curie != null ? curie : sug);
            }
            return suggestions;
        } else if (prefix != null) {
            Matcher m = CURIE_PATTERN.matcher(prefix);
            if (m.matches()) {
                String px = m.group(1);
                String local = m.group(2);

                if (px.equals(FUNCTION_PREFIX)) {
                    try {
                        final RepositoryConnection conn = sesameService.getConnection();
                        try {
                            conn.begin();
                            SesameConnectionBackend backend = SesameConnectionBackend.withConnection(conn);

                            final Set<SelectorFunction<Value>> functions = ldPathService.getFunctions();
                            List<String> suggestions = new ArrayList<String>();
                            for (SelectorFunction<Value> fn : functions) {
                                final String fName = fn.getPathExpression(backend);
                                if (fName.startsWith(local)) {
                                    suggestions.add(FUNCTION_PREFIX + ":" + fName + "()");
                                }
                            }
                            return suggestions;
                        } finally {
                            conn.commit();
                            conn.close();
                        }
                    } catch (RepositoryException e) {
                        return Collections.emptyList();
                    }
                } else if (prefixService.containsPrefix(px)) {
                    String resolved = prefixService.getNamespace(px) + (local != null ? local : "");
                    List<String> suggestions = new ArrayList<String>();
                    for (String c : getCompletions(resolved, limit, mode)) {
                        // CURIE urs MUST have a local part
                        if (c.length() <= resolved.length()) {
                            continue;
                        }
                        final String curie = prefixService.getCurie(c);
                        suggestions.add(curie != null ? curie : c);
                    }
                    return suggestions;
                }

            } else {
                List<String> suggestions = new ArrayList<String>();
                if (mode.equals(MODE_TRANSFORM)) {
                    for (String s : ldPathService.getTransformableTypes()) {
                        String px = prefixService.getPrefix(UriUtil.getNamespace(s));
                        if (px != null && px.startsWith(prefix) && !suggestions.contains(px)) {
                            suggestions.add(px);
                        }
                    }
                } else {
                    if (FUNCTION_PREFIX.startsWith(prefix)) {
                        suggestions.add(FUNCTION_PREFIX);
                    }
                    for (String px : prefixService.getMappings().keySet()) {
                        if (px.startsWith(prefix)) {
                            suggestions.add(px);
                        }
                    }
                }
                return suggestions;
            }

        }

        return Collections.emptyList();
    }

    private LDPathPrefixService createLocalPrefixService(UriInfo info) {
        HashMap<String, String> ns = new HashMap<String, String>();
        final MultivaluedMap<String, String> queryParameters = info.getQueryParameters();
        for (String key : queryParameters.keySet()) {
            if (key.startsWith("ns_")) {
                ns.put(key.substring(3), queryParameters.getFirst(key));
            }
        }
        return new LDPathPrefixService(ns);
    }

    private List<String> getCompletions(String uri, final int limit, String mode) {
        List<String> result = new ArrayList<String>();
        if (!mode.equals(MODE_TRANSFORM)) {
            try {
                RepositoryConnection con = sesameService.getConnection();
                try {
                    for (URI r : listResourcesByPrefix(con,uri, 0, limit)) {
                        result.add(r.stringValue());
                    }
                } finally {
                    con.commit();
                    con.close();
                }
            } catch (RepositoryException e) {
                handleRepositoryException(e,LDPathUtilWebService.class);
            }
        }
        for (String s : ldPathService.getTransformableTypes()) {
            if (s.startsWith(uri)) {
                result.add(s);
            }
        }
        return result;
    }

    @GET
    @Path("/path")
    @Produces(Namespaces.MIME_TYPE_JSON)
    public List<String> pathSuggestions(@QueryParam("path") String partialPath, @QueryParam("ctx") String[] ctx,
            @QueryParam("ctx[]") String[] ctx2, @Context UriInfo info) {
        final PrefixService prefixService = createLocalPrefixService(info);

        // Merge the contexts
        HashSet<String> context = new HashSet<String>();
        for (String c : ctx) {
            context.add(c);
        }
        for (String c : ctx2) {
            context.add(c);
        }

        // Clean the path
        String path = partialPath.replaceAll("/.*", "").trim();
        if (path.equals("")) {
            path = ".";
        }
        try {
            HashSet<URI> pathCandidates = new HashSet<URI>();
            try {
                RepositoryConnection con = sesameService.getConnection();
                try {
                    con.begin();
                    for (String rsc_uri : context) {
                        if (!ResourceUtils.isSubject(con, rsc_uri)) {
                            continue;
                        }

                        URI rsc = con.getValueFactory().createURI(rsc_uri);
                        Collection<Value> cPos = ldPathService.pathQuery(rsc, path, prefixService.getMappings());
                        for (Value cP : cPos) {
                            if (cP instanceof URI || cP instanceof BNode) {
                                for (Statement t : listOutgoing(con, (Resource) cP)) {
                                    pathCandidates.add(t.getPredicate());
                                }
                            }
                        }
                    }
                } finally {
                    con.commit();
                    con.close();
                }
            } catch (RepositoryException e) {
                handleRepositoryException(e,LDPathUtilWebService.class);
            }
            List<String> suggest = new ArrayList<String>();
            for (URI r : pathCandidates) {
                suggest.add(r.stringValue());
            }
            return suggest;
        } catch (LDPathParseException e) {
            // Silently fail.
        }
        return Collections.emptyList();
    }

    protected class LDPathPrefixService implements PrefixService {

        private final BiMap<String, String> localNS;

        public LDPathPrefixService(Map<String, String> local) {
            if (local != null) {
                this.localNS = HashBiMap.create(local);
            } else {
                this.localNS = HashBiMap.create();
            }
        }

        @Override
        public boolean containsPrefix(String prefix) {
            return localNS.containsKey(prefix) || prefixService.containsPrefix(prefix);
        }

        @Override
        public boolean containsNamespace(String namespace) {
            return localNS.containsValue(namespace) || prefixService.containsNamespace(namespace);
        }

        @Override
        public String getNamespace(String prefix) {
            if (localNS.containsKey(prefix))
                return localNS.get(prefix);
            else
                return prefixService.getNamespace(prefix);
        }

        @Override
        public String getPrefix(String namespace) {
            if (localNS.containsValue(namespace))
                return localNS.inverse().get(namespace);
            else
                return prefixService.getPrefix(namespace);
        }

        @Override
        public Map<String, String> getMappings() {
            Map<String, String> mappings = new HashMap<String, String>();
            mappings.putAll(prefixService.getMappings());
            mappings.putAll(localNS);
            return Collections.unmodifiableMap(mappings);
        }

        @Override
        public String getCurie(String uri) {
            if (UriUtil.validate(uri)) {
                String ns = UriUtil.getNamespace(uri);
                String ref = UriUtil.getReference(uri);
                if (StringUtils.isNotBlank(ns) && StringUtils.isNotBlank(ref) && containsNamespace(ns))
                    return getPrefix(ns) + ":" + ref;
                else
                    return null;
            } else
                return null;
        }

        @Override
        public void add(String prefix, String namespace) throws IllegalArgumentException, URISyntaxException {
            // nop;
        }

        @Override
        public void forceAdd(String prefix, String namespace) {
            // nop;
        }
        
		@Override
		public boolean remove(String prefix) {
			return false; // nop;
		}

        @Override
        public String serializePrefixMapping() {
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> mapping : getMappings().entrySet()) {
                sb.append("\n").append(mapping.getKey()).append(": ").append(mapping.getValue()).append(" ");
            }
            return sb.toString();
        }

        @Override
        public String serializePrefixesSparqlDeclaration() {
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> mapping : getMappings().entrySet()) {
                sb.append("PREFIX ").append(mapping.getKey()).append(": <").append(mapping.getValue()).append("> \n");
            }
            return sb.toString();
        }

    }

}
