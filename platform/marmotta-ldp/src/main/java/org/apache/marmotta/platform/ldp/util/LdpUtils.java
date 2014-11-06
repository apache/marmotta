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
package org.apache.marmotta.platform.ldp.util;

import info.aduna.iteration.CloseableIteration;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.platform.ldp.api.Preference;
import org.apache.marmotta.platform.ldp.webservices.PreferHeader;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriter;
import org.slf4j.Logger;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Various Util-Methods for the {@link org.apache.marmotta.platform.ldp.api.LdpService}.
 */
public class LdpUtils {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(LdpUtils.class);

    /**
     * Urify the Slug: header value, i.e. replace all non-url chars with a single dash.
     *
     * @param slugHeaderValue the client-provided Slug-header
     * @return the slugHeaderValue "urified"
     */
    public static String urify(String slugHeaderValue) {
        return slugHeaderValue.trim()
                // Replace non-url chars with '-'
                .replaceAll("[^\\w]+", "-");
    }

    /**
     * Get the preferred file extension for the content type
     *
     * @param mediaType content type
     * @return file extension (already including '.')
     */
    public static String getExtension(MediaType mediaType) {
        String contentType = getMimeType(mediaType);
        return getExtension(contentType);
    }

    /**
     * Get the preferred file extension for the content type
     *
     * @param mimeType mimeType
     * @return file extension (already including '.')
     */
    public static String getExtension(String mimeType) {
        final String defaultExt = ".bin";
        final MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        try {
            final String ext = allTypes.forName(mimeType).getExtension();
            log.trace("Tika's file-extension for {} is '{}'", mimeType, ext);
            if (StringUtils.isNotBlank(ext)) {
                return ext;
            }
        } catch (MimeTypeException e) {
            log.trace("MimeTypeException: {}. Not critical, recovering...", e.getMessage());
        }
        log.trace("Using fallback file-extension '{}' for {}", defaultExt, mimeType);
        return defaultExt;
    }

    /**
     * Get <b>only</b> the mimeType from the {@link javax.ws.rs.core.MediaType}
     *
     * @param mediaType the mediaType
     * @return the mimeType
     */
    public static String getMimeType(MediaType mediaType) {
        return (mediaType != null ? String.format("%s/%s", mediaType.getType(), mediaType.getSubtype()) : "");
    }

    /**
     * LDP-Style to serialize a resource.
     *
     * @param writer the writer to serialize to
     * @param subject the resource to serialize
     * @param iteration the Iteration containing the data
     * @throws RDFHandlerException
     * @throws RepositoryException
     */
    public static void exportIteration(RDFWriter writer, URI subject, CloseableIteration<Statement, RepositoryException> iteration) throws RDFHandlerException, RepositoryException {
        writer.startRDF();

        writer.handleNamespace(LDP.PREFIX, LDP.NAMESPACE);
        writer.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
        writer.handleNamespace(XSD.PREFIX, XSD.NAMESPACE);
        writer.handleNamespace(DCTERMS.PREFIX, DCTERMS.NAMESPACE);

        writer.handleNamespace("parent", subject.getNamespace());
        writer.handleNamespace("child", subject.stringValue().replaceFirst("/*$", "/"));
        writer.handleNamespace("this", subject.stringValue().replaceFirst("/*$", "#"));

        while (iteration.hasNext()) {
            writer.handleStatement(iteration.next());
        }

        writer.endRDF();
    }

    public static String getAcceptPostHeader(String extraFormats) {
        final Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
        final StringBuilder sb = new StringBuilder();
        for (RDFFormat rdfFormat : rdfFormats) {
            sb.append(rdfFormat.getDefaultMIMEType());
            sb.append(", ");
        }
        if (StringUtils.isNotBlank(extraFormats)) {
            sb.append(extraFormats);
        } else {
            sb.delete(sb.length()-2, sb.length());
        }
        return sb.toString();
    }

    public static String getContainer(String resource) throws MalformedURLException, URISyntaxException {
        java.net.URI uri = new java.net.URI(resource);
        java.net.URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
        return parent.toASCIIString();
    }

    public static URI getContainer(URI resource) throws MalformedURLException, URISyntaxException {
        return new URIImpl(resource.getNamespace());
    }

    /**
     * Convert a PreferHeader into a LDP Preference.
     * @param prefer the PreferHeader to parse
     * @return the Preference
     */
    public static Preference parsePreferHeader(PreferHeader prefer) {
        if (prefer == null) return null;
        // we only support "return"-prefers
        if (!PreferHeader.PREFERENCE_RETURN.equals(prefer.getPreference())) {
            return null;
        }
        if (PreferHeader.RETURN_MINIMAL.equals(prefer.getPreferenceValue())) {
            return Preference.minimalPreference();
        }

        if (PreferHeader.RETURN_REPRESENTATION.equals(prefer.getPreferenceValue())) {
            final String include = prefer.getParamValue(PreferHeader.RETURN_PARAM_INCLUDE);
            final String omit = prefer.getParamValue(PreferHeader.RETURN_PARAM_OMIT);
            if (StringUtils.isNotBlank(include) && StringUtils.isBlank(omit)) {
                return Preference.includePreference(include.split("\\s+"));
            } else if (StringUtils.isNotBlank(omit) && StringUtils.isBlank(include)) {
                return Preference.omitPreference(omit.split("\\s+"));
            } else if (StringUtils.isBlank(include) && StringUtils.isBlank(omit)) {
                return Preference.defaultPreference();
            } else {
                return null;
            }
        }
        return null;
    }

    private LdpUtils() {
        // Static access only
    }

}
