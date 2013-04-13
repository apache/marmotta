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
package org.apache.marmotta.platform.core.rio;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.io.RDFHtmlWriter;
import org.apache.marmotta.platform.core.api.io.RDFWriterPriority;
import org.apache.marmotta.platform.core.api.prefix.PrefixService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.util.CDIContext;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RDF to HTML Writer
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class RDFHtmlWriterImpl implements RDFHtmlWriter {

    protected final String TEMPLATE = "rdfhtml.ftl";

    protected ConfigurationService configurationService;

    protected PrefixService prefixService;
    
    protected TemplatingService templatingService;

    protected Logger log = LoggerFactory.getLogger(RDFHtmlWriterImpl.class);

    protected PrintWriter writer;

    protected Map<Resource, SortedSet<Statement>> tripleMap = new HashMap<Resource, SortedSet<Statement>>();

    protected Map<String, String> namespaceMap = new HashMap<String, String>();

    public RDFHtmlWriterImpl(OutputStream out) {
        this(new OutputStreamWriter(out));
    }

    public RDFHtmlWriterImpl(Writer writer) {
        this(new PrintWriter(writer));
    }

    public RDFHtmlWriterImpl(PrintWriter writer) {
        super();
        this.writer = writer;

        // FIXME: usage of KiWiContext is not recommened!
        configurationService = CDIContext
                .getInstance(ConfigurationService.class);
        prefixService = CDIContext.getInstance(PrefixService.class);
        templatingService = CDIContext.getInstance(TemplatingService.class);
    }

    /**
     * Gets the RDF format that this RDFWriter uses.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFHtmlFormat.FORMAT;
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     * 
     * @throws org.openrdf.rio.RDFHandlerException
     *             If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {

    }

    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     * 
     * @throws org.openrdf.rio.RDFHandlerException
     *             If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {

        List<Map<String, Object>> resources = new ArrayList<Map<String, Object>>();
        for (Map.Entry<Resource, SortedSet<Statement>> entry : tripleMap
                .entrySet()) {
            SortedSet<Statement> ts = entry.getValue();
            Map<String, Object> resource = new HashMap<String, Object>();
            String uri = ts.first().getSubject().stringValue();
            resource.put("uri", uri);
            try {
                resource.put("encoded_uri", URLEncoder.encode(uri, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("Error trying to encode '" + uri + "': "
                        + e.getMessage());
                resource.put("encoded_uri", uri);
            }

            List<Map<String, Object>> triples = new ArrayList<Map<String, Object>>();
            for (Statement t : ts) {
                Map<String, Object> triple = new HashMap<String, Object>();

                Map<String, String> predicate = new HashMap<String, String>();
                String predicateUri = t.getPredicate().stringValue();
                predicate.put("uri", predicateUri);
                String predicateCurie = prefixService.getCurie(predicateUri);
                predicate.put("curie",
                        StringUtils.isNotBlank(predicateCurie) ? predicateCurie
                                : predicateUri);
                triple.put("predicate", predicate);
                predicate = null;

                Map<String, String> object = new HashMap<String, String>();
                Value value = t.getObject();
                String objectValue = value.stringValue();
                if (value instanceof URI) {
                    object.put("uri", objectValue);
                    String objectCurie = prefixService.getCurie(objectValue);
                    object.put("curie",
                            StringUtils.isNotBlank(objectCurie) ? objectCurie
                                    : objectValue);
                    object.put("cache", "true");
                } else if (value instanceof Literal) {
                    Literal literal = (Literal) t.getObject();
                    String lang = literal.getLanguage();
                    if (StringUtils.isNotBlank(lang)) {
                        object.put("lang", lang);
                        objectValue = "\"" + objectValue + "\"@" + lang;
                        if (literal.getDatatype() != null) {
                            String datatype = prefixService.getCurie(literal
                                    .getDatatype().stringValue());
                            object.put("datatype", datatype);
                            objectValue += "^^" + datatype;
                        }
                    } else {
                        if (literal.getDatatype() != null) {
                            String datatype = prefixService.getCurie(literal
                                    .getDatatype().stringValue());
                            object.put("datatype", datatype);
                            objectValue = "\"" + objectValue + "\"^^"
                                    + datatype;
                        }
                    }
                    object.put("value", objectValue);
                } else {
                    object.put("value", objectValue);
                }
                triple.put("object", object);
                object = null;

                Map<String, String> context = new HashMap<String, String>();
                String contextUri = t.getContext().stringValue();
                context.put("uri", contextUri);
                String contextCurie = prefixService.getCurie(contextUri);
                context.put("curie",
                        StringUtils.isNotBlank(contextCurie) ? contextCurie
                                : contextUri);
                triple.put("context", context);
                context = null;

                if (t instanceof KiWiTriple && ((KiWiTriple) t).isInferred()) {
                    triple.put("info", createInfo(((KiWiTriple) t).getId()));
                } else {
                    triple.put("info", "-");
                }

                triples.add(triple);
            }
            resource.put("triples", triples);
            resources.add(resource);
        }

        try {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("baseUri", configurationService.getServerUri());
            data.put("resources", resources);
            data.put("prefixMappings", prefixService.serializePrefixMapping());

            //set timemap link
            if(configurationService.getBooleanConfiguration("versioning.enabled")) {
                data.put("timemaplink", configurationService.getStringConfiguration("versioning.memento.timemap"));
            }

            templatingService.process(TEMPLATE, data, writer);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RDFHandlerException(e);
        }

    }

    /**
     * Handles a namespace declaration/definition. A namespace declaration
     * associates a (short) prefix string with the namespace's URI. The prefix
     * for default namespaces, which do not have an associated prefix, are
     * represented as empty strings.
     * 
     * @param prefix
     *            The prefix for the namespace, or an empty string in case of a
     *            default namespace.
     * @param uri
     *            The URI that the prefix maps to.
     * @throws org.openrdf.rio.RDFHandlerException
     *             If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleNamespace(String prefix, String uri)
            throws RDFHandlerException {
        namespaceMap.put(uri, prefix);
    }

    /**
     * Handles a statement.
     * 
     * @param st
     *            The statement.
     * @throws org.openrdf.rio.RDFHandlerException
     *             If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        if (tripleMap.containsKey(st.getSubject())) {
            SortedSet<Statement> ts = tripleMap.get(st.getSubject());
            ts.add(st);
        } else {
            SortedSet<Statement> ts = new TreeSet<Statement>(
                    new Comparator<Statement>() {
                        @Override
                        public int compare(Statement o1, Statement o2) {
                            if (o1.getPredicate().toString()
                                    .equals(o2.getPredicate().toString()))
                                return -1; // FIXME: jfrank: why this?
                            return o1.getPredicate().toString()
                                    .compareTo(o2.getPredicate().toString());
                        }
                    });
            ts.add(st);
            tripleMap.put(st.getSubject(), ts);
        }
    }

    /**
     * Handles a comment.
     * 
     * @param comment
     *            The comment.
     * @throws org.openrdf.rio.RDFHandlerException
     *             If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    private String createInfo(long id) { // TODO: move to freemarker too
        StringBuilder sb = new StringBuilder();
        String closer = "<button style='position:absolute;top:5px;right:5px;z-index:2;' onclick='document.getElementById(\"info"
                + id + "\").style.display = \"none\"'>X</button>";
        String style = "border:1px solid black; width:50%; position:absolute;top:100px;left:25%;background-color:white;z-index:2;display:none;padding-top:10px;min-height:100px;overflow:auto;";
        String iframe = "<iframe id='iframe" + id
                + "' src='' style='border:none;width:100%'></iframe>";
        sb.append("<a href='#' onclick='document.getElementById(\"iframe")
                .append(id).append("\").src=\"")
                .append(configurationService.getServerUri())
                .append("core/public/html/reasoning.html#").append(id)
                .append("\";document.getElementById(\"info").append(id)
                .append("\").style.display = \"block\";'>info</a>");
        sb.append("<div style='").append(style).append("' id='info").append(id)
                .append("'>");
        sb.append(iframe).append(closer);
        sb.append("</div>");
        return sb.toString();
    }

    @Override
    public RDFWriterPriority getPriority() {
        return RDFWriterPriority.MEDIUM;
    }

}
