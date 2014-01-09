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
package org.apache.marmotta.ldclient.provider.mediawiki;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathFactory;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MediawikiProvider allows direct triplification of Mediawiki Articles and Categories.
 * <p/>
 * For real flexible Endpoint/Provider configuration see
 * {@link #buildEndpointUrl(String, Endpoint)}
 *
 * @see <a href="http://www.mediawiki.org/wiki/API:Main_page">Mediawiki API</a>
 * @see #buildEndpointUrl(String, Endpoint)
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 *
 */
public class MediawikiProvider extends AbstractHttpProvider {

    public static final String   PROVIDER_NAME        = "MediaWiki Provider";
    private static Logger        log                  = LoggerFactory.getLogger(MediawikiProvider.class);

    private static final Pattern COLON_PREFIX_PATTERN = Pattern.compile("^[^:]*:");
    private static final Pattern REDIRECT_PATTERN     = Pattern.compile("^#REDIRECT\\s*\\[\\[([^#\\]]*)(#.*)?\\]\\]",
            Pattern.CASE_INSENSITIVE);
    private static final int     MAX_TITLES_PER_QUERY = 50;


    private static enum Context {
        META, META_CONTINUED, CONTENT, LINKS, CATEGORIES, SUBCATEGORIES, SUPERCATEGORIES, PAGES, REDIRECT,
        CATEGORYMEMBERS_PAGES, CATEGORYMEMBERS_SUBCATS, SITE, NO_CONTEXT;
        private static final String paramName = "context";

        public static Context fromUrl(String url) {
            try {
                for (String param : url.split("[?&]")) {
                    if (param.startsWith(paramName + "=")) return valueOf(param.substring(paramName.length() + 1));
                }
            } catch (Exception w) {
            }
            return NO_CONTEXT;
        }

        public String urlParam() {
            return paramName + "=" + this.toString();
        }
    }

    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    @Override
    public String[] listMimeTypes() {
        return new String[] { "text/xml" };
    }

    @Override
    public List<String> buildRequestUrl(String resource, Endpoint endpoint) throws DataRetrievalException {

        String _t = parseParams(resource).get("title");
        if (_t == null) {
            final String u = resource.replaceAll("\\?.*", "");
            _t = u.substring(u.lastIndexOf('/') + 1);
        }
        final String pageTitle = _t;
        final String apiUrl = buildEndpointUrl(resource, endpoint);
        if (pageTitle != null && !pageTitle.equals("")) {
            final HashMap<String, String> params = new LinkedHashMap<String, String>();

            params.putAll(getDefaultParams("info", null));
            params.putAll(getDefaultParams("revisions", null));
            params.putAll(getDefaultParams("categories", null));
            params.putAll(getDefaultParams("links", null));

            final String metaUrl = buildApiPropQueryUrl(apiUrl, pageTitle, "info|revisions|categories|links", params,
                    Context.META);

            params.clear();
            params.putAll(getDefaultParams("info", null));
            params.putAll(getDefaultParams("revisions", null));

            params.put("rvdir", "older");
            params.put("rvprop", "ids|timestamp|content");
            params.put("inprop", "url|preload");
            final String contentUrl = buildApiPropQueryUrl(apiUrl, pageTitle, "info|revisions", params, Context.CONTENT);

            final List<String> urls = Arrays.asList(metaUrl, contentUrl);
            if (log.isTraceEnabled()) {
                log.trace("CACHE {}", resource);
                for (String u : urls) {
                    log.trace("RESOLVE {}", urlDecode(u).replaceFirst(".*=xml&", ""));
                }
            }
            return urls;
        } else {
            // This seems like the Wiki
            Map<String, String> params = new LinkedHashMap<String, String>();
            params.put("action", "query");
            params.put("meta", "siteinfo");
            params.put("siprop", "general");

            final String siteUrl = buildApiRequestUrl(apiUrl, params, Context.SITE);
            if (log.isTraceEnabled()) {
                log.trace("CACHE {}", resource);
                log.trace("RESOLVE {}", siteUrl);
            }
            return Collections.singletonList(siteUrl);
        }
    }

    /**
     * Build the endpoint url based on
     * <ol>
     * <li>the URI of the resource
     * <li>the {@link Endpoint#getUriPattern()}
     * <li>the {@link Endpoint#getEndpointUrl()}
     * </ol>
     * <p/>
     * Iff the uriPattern <em>entirely</em> (not only a prefix) matches the resourceUri, the apiUrl
     * is build as <code>apiURL = resourceUri.replaceAll(uriPattern, endpointUrl)</code>; else it's
     * just <code>endpointUrl</code>
     *
     *
     * @param resource the resource to retrieve
     * @param endpoint the endpoint to use
     * @return the url to the Mediawiki API for this resouces
     */
    protected String buildEndpointUrl(String resource, Endpoint endpoint) {
        final Pattern pattern = endpoint.getUriPatternCompiled();
        if (pattern != null) {
            final Matcher matcher = pattern.matcher(resource);
            if (matcher.matches()) return matcher.replaceFirst(endpoint.getEndpointUrl());
        }
        return endpoint.getEndpointUrl();
    }

    @Override
    public List<String> parseResponse(String resource, String requestUrl, Model model, InputStream in, String contentType)
            throws DataRetrievalException {
        try {
            log.trace("PARSE {}", urlDecode(requestUrl).replaceFirst(".*=xml&", ""));

            parseParams(requestUrl);
            final Context context = Context.fromUrl(requestUrl);
            final Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(in);

            ArrayList<String> followUp = new ArrayList<String>();
            final ValueFactory valueFactory = new ValueFactoryImpl();

            switch (context) {
                case SITE:
                    followUp.addAll(parseSiteMeta(resource, requestUrl, doc, model));
                    break;
                case META:
                case META_CONTINUED:
                    // For Articles: Retrieve article info (sioc:WikiArticle)
                    // Follow-Up: resolve titles: links, categories
                    // For Categories: Retrieve as skos:Concept/sioc:Category
                    followUp.addAll(parseArticleMeta(resource, requestUrl, doc, context, model));
                    break;
                case CONTENT:
                    followUp.addAll(parseRevision(valueFactory.createURI(resource), requestUrl, model, valueFactory, queryElement(doc, "/api/query/pages/page[1]/revisions"),
                            context));
                    break;
                /* Links from an Article */
                case REDIRECT:
                case LINKS:
                case CATEGORIES:
                /* Links from a Category */
                case SUPERCATEGORIES:
                case SUBCATEGORIES:
                case PAGES:
                    followUp.addAll(addLinks(resource, requestUrl, doc, context, model));
                    break;
                default:
                    log.error("Unhandled MediawikiProvider.Context: {}", context);
                    break;
            }

            if (log.isTraceEnabled()) {
                for (String f : followUp) {
                    log.trace("FOLLOW {}", urlDecode(f).replaceFirst(".*=xml&", ""));
                }
            }
            return followUp;

            // throw new DataRetrievalException("Invalid response from Mediawiki-API.");
        } catch (RepositoryException e) {
            throw new DataRetrievalException("repository error while parsing XML response", e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        }
    }

    protected List<String> parseSiteMeta(String resource, String requestUrl, Document doc, Model model) throws RepositoryException {
        final Element general = queryElement(doc, "/api/query/general");
        if (general != null) {
            final String title = general.getAttributeValue("sitename");
            final String server = general.getAttributeValue("server");
            final String homepage = general.getAttributeValue("base");

            final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
            final Resource subject = valueFactory.createURI(resource);

            if (title != null) {
                addLiteralTriple(subject, Namespaces.NS_DC_TERMS + "title", title, null, model, valueFactory);
            }
            if (server != null) {
                if (server.matches("^https?://")) {
                    addTriple(subject, Namespaces.NS_SIOC + "has_host", server, model, valueFactory);
                } else if (server.startsWith("//")) {
                    addTriple(subject, Namespaces.NS_SIOC + "has_host", "http:" + server, model, valueFactory);
                } else {
                    log.warn("Found invalid host {} for wiki {}, ignoring", server, resource);
                }
            }
            if (homepage != null) {
                addTriple(subject, Namespaces.NS_FOAF + "homepage", homepage, model, valueFactory);
            }
            addTypeTriple(subject, Namespaces.NS_SIOC_TYPES + "Wiki", model, valueFactory);
        }

        return Collections.emptyList();
    }

    protected List<String> addLinks(String resource, String requestUrl, Document doc, Context context, Model model)
            throws RepositoryException {
        final String predicate;
        switch (context) {
            case LINKS:
                predicate = Namespaces.NS_SIOC + "links_to";
                break;
            case CATEGORIES:
                predicate = Namespaces.NS_SIOC + "topic";
                break;
            case REDIRECT:
                predicate = Namespaces.NS_DC_TERMS + "isReplacedBy";
                break;
            case SUBCATEGORIES:
                predicate = Namespaces.NS_SKOS + "narrower";
                break;
            case SUPERCATEGORIES:
                predicate = Namespaces.NS_SKOS + "broader";
                break;
            case PAGES:
                predicate = Namespaces.NS_SIOC + "topic"; /* INVERSE !!! */
                break;
            default:
                return Collections.emptyList();
        }

        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        final Resource subject = valueFactory.createURI(resource);

        for (Element page : queryElements(doc, "/api/query/pages/page")) {
            if (page.getAttributeValue("missing") != null) {
                continue;
            }

            final String url = page.getAttributeValue("fullurl");
            if (url != null) {
                if (context == Context.PAGES) {
                    addTriple(valueFactory.createURI(url), predicate, resource, model, valueFactory);
                } else {
                    addTriple(subject, predicate, url, model, valueFactory);
                }
            }
        }
        return Collections.emptyList();
    }

    protected List<String> parseArticleMeta(String resource, String requestUrl, Document doc, Context context,
                                            Model model) throws RepositoryException {
        ArrayList<String> followUp = new ArrayList<String>();

        Element page = queryElement(doc, "/api/query/pages/page[1]");
        if (page != null) {
            if (page.getAttributeValue("missing") != null) return Collections.emptyList();

            final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
            final URI subject = valueFactory.createURI(resource);

            final String title = page.getAttributeValue("title");
            final String pageId = page.getAttributeValue("pageid");
            final String namespace = page.getAttributeValue("ns");
            final String ident = namespace + ":" + pageId;

            final String wiki = page.getAttributeValue("fullurl");
            if (wiki != null) {
                String wikiUrl = wiki.replaceFirst("(?i:" + Pattern.quote(title.replaceAll(" ", "_")) + ")$", "");
                addTriple(subject, Namespaces.NS_SIOC + "has_container", wikiUrl, model, valueFactory);
            }

            if (context == Context.META) {
                if ("0".equals(namespace)) {
                    addTypeTriple(subject, Namespaces.NS_SIOC_TYPES + "WikiArticle", model, valueFactory);
                    addLiteralTriple(subject, Namespaces.NS_DC_TERMS + "title", title, null, model, valueFactory);
                } else if ("14".equals(namespace)) {
                    // Category is a subType of skoc:Concept
                    addTypeTriple(subject, Namespaces.NS_SIOC_TYPES + "Category", model, valueFactory);
                    Matcher m = COLON_PREFIX_PATTERN.matcher(title);
                    if (m.find()) {
                        addLiteralTriple(subject, Namespaces.NS_SKOS + "prefLabel", m.replaceFirst(""), null, model,
                                valueFactory);
                        addLiteralTriple(subject, Namespaces.NS_SKOS + "altLabel", title, null, model,
                                valueFactory);
                    } else {
                        addLiteralTriple(subject, Namespaces.NS_SKOS + "prefLabel", title, null, model,
                                valueFactory);
                    }

                    // In a category, we also want sub-categories and contained pages
                    final Map<String, String> cmParams = getDefaultParams("categorymembers", null);
                    cmParams.put("cmtitle", title);
                    followUp.add(buildApiListQueryUrl(requestUrl, "categorymembers", cmParams, Context.META));
                }

                addLiteralTriple(subject, Namespaces.NS_DC_TERMS + "identifier", ident, Namespaces.NS_XSD + "string", model, valueFactory);
                addLiteralTriple(subject, Namespaces.NS_DC_TERMS + "modified", page.getAttributeValue("touched"), Namespaces.NS_XSD
                        + "dateTime", model, valueFactory);
                followUp.addAll(parseRevision(subject, requestUrl, model, valueFactory, page.getChild("revisions"), Context.META));
                if (page.getAttributeValue("fullurl") != null && !resource.equals(page.getAttributeValue("fullurl"))) {
                    addTriple(subject, Namespaces.NS_OWL + "sameAs", page.getAttributeValue("fullurl"), model, valueFactory);
                }
            }

            List<Element> pagings = queryElements(doc, "/api/query-continue/*");
            for (int i = 0; i < pagings.size(); i++) {
                Element e = pagings.get(i);
                if (!"revisions".equals(e.getName())) {
                    followUp.add(buildApiPropQueryUrl(requestUrl, title, e.getName(), getDefaultParams(e.getName(), e),
                            Context.META_CONTINUED));
                }
            }

            // Parse categories of this article, and resolve them in a followUp request
            final Element cats = page.getChild("categories");
            if (cats != null) {
                final List<Element> cls = cats.getChildren("cl");
                if (cls.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < cls.size(); i++) {
                        sb.append(cls.get(i).getAttributeValue("title")).append("|");
                        if ((i + 1) % MAX_TITLES_PER_QUERY == 0) {
                            final String titles = sb.substring(0, sb.length() - 1);
                            if ("0".equals(namespace)) {
                                followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.CATEGORIES));
                            } else if ("14".equals(namespace)) {
                                followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.SUPERCATEGORIES));
                            }
                            sb = new StringBuilder();
                        }
                    }
                    if (sb.length() > 0) {
                        final String titles = sb.substring(0, sb.length() - 1);
                        if ("0".equals(namespace)) {
                            followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.CATEGORIES));
                        } else if ("14".equals(namespace)) {
                            followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.SUPERCATEGORIES));
                        }
                    }
                }
            }

            // Parse (page)links of this article, and resolve them in a followUp request
            final Element links = page.getChild("links");
            if (links != null) {
                final List<Element> pls = links.getChildren("pl");
                if (pls.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < pls.size(); i++) {
                        sb.append(pls.get(i).getAttributeValue("title")).append("|");
                        if ((i + 1) % MAX_TITLES_PER_QUERY == 0) {
                            final String titles = sb.substring(0, sb.length() - 1);
                            followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.LINKS));
                            sb = new StringBuilder();
                        }
                    }
                    if (sb.length() > 0) {
                        final String titles = sb.substring(0, sb.length() - 1);
                        followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.LINKS));
                    }
                }
            }

        }
        Element catmembers = queryElement(doc, "/api/query/categorymembers");
        if (catmembers != null) {
            final List<Element> cms = catmembers.getChildren("cm");
            if (cms.size() > 0) {
                StringBuilder pt = new StringBuilder(), ct = new StringBuilder();
                int ptC = 0, ctC = 0;
                for (int i = 0; i < cms.size(); i++) {
                    final Element cm = cms.get(i);
                    final String type = cm.getAttributeValue("type");
                    final String title = cm.getAttributeValue("title");
                    if ("page".equals(type)) {
                        pt.append(title).append("|");
                        ptC++;
                    } else if ("subcat".equals(type)) {
                        ct.append(title).append("|");
                        ctC++;
                    } else if (type == null) {
                        // attribute type is available from Mediawiki 1.18+, use this as fallback.
                        final String ns = cm.getAttributeValue("ns");
                        if ("0".equals(ns)) {
                            pt.append(title).append("|");
                            ptC++;
                        } else if ("14".equals(ns)) {
                            ct.append(title).append("|");
                            ctC++;
                        }
                    }
                    if (ptC > MAX_TITLES_PER_QUERY) {
                        final String titles = pt.substring(0, pt.length() - 1);
                        followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.PAGES));
                        ptC = 0;
                        pt = new StringBuilder();
                    }
                    if (ctC > MAX_TITLES_PER_QUERY) {
                        final String titles = ct.substring(0, ct.length() - 1);
                        followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.SUBCATEGORIES));
                        ctC = 0;
                        ct = new StringBuilder();
                    }
                }
                if (ptC > 50) {
                    final String titles = pt.substring(0, pt.length() - 1);
                    followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.PAGES));
                }
                if (ctC > 50) {
                    final String titles = ct.substring(0, ct.length() - 1);
                    followUp.add(buildApiPropQueryUrl(requestUrl, titles, "info", getDefaultParams("info", null), Context.SUBCATEGORIES));
                }

            }
            Element cmContinue = queryElement(doc, "/api/query-continue/categorymembers");
            if (cmContinue != null) {
                final Map<String, String> cmParams = getDefaultParams(cmContinue.getName(), cmContinue);
                cmParams.put("cmtitle", parseParams(requestUrl).get("cmtitle"));
                followUp.add(buildApiListQueryUrl(requestUrl, cmContinue.getName(), cmParams, Context.META_CONTINUED));
            }
        }
        return followUp;
    }

    protected List<String> parseRevision(URI resource, String requestUrl, Model model, ValueFactory valueFactory,
                                         Element revisions, Context context) throws RepositoryException {
        List<String> followUp = Collections.emptyList();
        if (revisions == null) return followUp;
        final Element rev = revisions.getChild("rev");
        if (rev == null) return followUp;
        final Resource subject = resource;

        if (context == Context.META && "0".equals(rev.getAttributeValue("parentid"))) {
            // This is the first revision, so we use the creation date
            addLiteralTriple(subject, Namespaces.NS_DC_TERMS + "created", rev.getAttributeValue("timestamp"), Namespaces.NS_XSD + "dateTime", model,
                    valueFactory);
        }
        if (context == Context.CONTENT && rev.getValue() != null && rev.getValue().trim().length() > 0) {
            final String content = rev.getValue().trim();
            final Matcher m = REDIRECT_PATTERN.matcher(content);
            if (((Element) revisions.getParent()).getAttribute("redirect") != null && m.find()) {
                followUp = Collections.singletonList(buildApiPropQueryUrl(requestUrl, m.group(1), "info", getDefaultParams("info", null),
                        Context.REDIRECT));
            } else {
                addLiteralTriple(subject, Namespaces.NS_RSS_CONTENT + "encoded", content, Namespaces.NS_XSD + "string", model, valueFactory);
            }
        }
        return followUp;
    }

    private static String buildApiListQueryUrl(String url, String list, Map<String, String> extraArgs, Context context) {
        HashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "query");
        params.put("list", list);

        params.putAll(extraArgs);

        return buildApiRequestUrl(url, params, context);
    }

    private static String buildApiPropQueryUrl(String url, String titleQuery, String prop, Map<String, String> extraArgs, Context context) {
        HashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "query");
        params.put("titles", titleQuery);
        params.put("prop", prop);

        params.putAll(extraArgs);

        return buildApiRequestUrl(url, params, context);
    }

    private static String buildApiRequestUrl(String url, Map<String, String> params, Context context) {
        StringBuilder sb = new StringBuilder(url.replaceAll("\\?.*", ""));
        sb.append("?format=xml");

        for (String param : params.keySet()) {
            sb.append("&").append(urlEncode(param));
            sb.append("=").append(urlEncode(params.get(param)));
        }
        if (context != null && context != Context.NO_CONTEXT) {
            sb.append("&").append(context.urlParam());
        }

        return sb.toString();
    }

    private static Map<String, String> getDefaultParams(String prop, Element queryContinue) {
        HashMap<String, String> params = new LinkedHashMap<String, String>();
        final String limit = "max";

        if ("info".equals(prop)) {
            params.put("inprop", "url");
        } else if ("revisions".equals(prop)) {
            // Revision info: first revision for creation
            params.put("rvdir", "newer");
            params.put("rvlimit", "1");
            params.put("rvprop", "ids|timestamp");
        } else if ("categories".equals(prop)) {
            params.put("cllimit", limit);
            // Categories: only visible cats
            params.put("clshow", "!hidden");
        } else if ("links".equals(prop)) {
            params.put("pllimit", limit);
            // Links: only links to same
            params.put("plnamespace", "0");
        } else if ("categorymembers".equals(prop)) {
            params.put("cmlimit", limit);
            params.put("cmprop", "title|type");
        }

        if (queryContinue != null && queryContinue.getName().equals(prop) && queryContinue.getAttributes().size() == 1) {
            final Attribute a = queryContinue.getAttributes().get(0);
            params.put(a.getName(), a.getValue());
        }

        return params;
    }

    private static String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    private static String urlDecode(String string) {
        try {
            return URLDecoder.decode(string, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    protected Map<String, String> parseParams(String requestUrl) {
        if (requestUrl.indexOf('?') < 0) return Collections.emptyMap();
        final String queryString = requestUrl.substring(requestUrl.indexOf('?') + 1);
        HashMap<String, String> params = new LinkedHashMap<String, String>();
        for (String param : queryString.split("&")) {
            final String kv[] = param.split("=");
            if (kv.length == 2) {
                params.put(urlDecode(kv[0]), urlDecode(kv[1]));
            } else if (kv.length == 1) {
                params.put(urlDecode(kv[0]), Boolean.TRUE.toString());
            }
        }
        return params;

    }

    private static void addTriple(Resource subject, String predicate, String object, Model model, ValueFactory valueFactory)
            throws RepositoryException {
        if (predicate == null || object == null) return;
        final URI predUri = valueFactory.createURI(predicate);
        final URI objUri = valueFactory.createURI(object);

        Statement stmt = valueFactory.createStatement(subject, predUri, objUri);
        model.add(stmt);

    }

    private static void addLiteralTriple(Resource subject, String predicate, String label, String datatype, Model model,
                                  ValueFactory valueFactory) throws RepositoryException {
        if (predicate == null || label == null) return;
        final URI predUri = valueFactory.createURI(predicate);

        final Literal lit;
        if (datatype != null) {
            final URI dType = valueFactory.createURI(datatype);
            lit = valueFactory.createLiteral(label, dType);
        } else {
            lit = valueFactory.createLiteral(label);
        }

        Statement stmt = valueFactory.createStatement(subject, predUri, lit);
        model.add(stmt);

    }

    private static void addTypeTriple(Resource subject, String type, Model model, ValueFactory valueFactory) throws RepositoryException {
        if (type == null) return;
        final URI predUri = valueFactory.createURI(Namespaces.NS_RDF + "type");
        final URI rdfType = valueFactory.createURI(type);

        Statement stmt = valueFactory.createStatement(subject, predUri, rdfType);
        model.add(stmt);

    }

    protected static Element queryElement(Document n, String query) {
        return XPathFactory.instance().compile(query, new ElementFilter()).evaluateFirst(n);
    }

    protected static List<Element> queryElements(Document n, String query) {
        return XPathFactory.instance().compile(query, new ElementFilter()).evaluate(n);
    }

}
