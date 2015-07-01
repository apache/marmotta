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
import info.aduna.lang.FileFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.apache.marmotta.platform.ldp.api.Preference;
import org.apache.marmotta.platform.ldp.webservices.PreferHeader;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.slf4j.Logger;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        writer.handleNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
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
        final Collection<RDFFormat> rdfFormats = filterAvailableParsers(LdpService.SERVER_PREFERED_RDF_FORMATS);
        final StringBuilder sb = new StringBuilder();
        for (RDFFormat rdfFormat : rdfFormats) {
            sb.append(rdfFormat.getDefaultMIMEType());
            sb.append(", ");
        }
        if (StringUtils.isNotBlank(extraFormats)) {
            sb.append(extraFormats);
        } else if (sb.length() > 1) {
            sb.delete(sb.length()-2, sb.length());
        }
        return sb.toString();
    }

    /**
     * Tries to match the specified MIME type with the MIME types of the supplied
     * file formats.
     * This method does exactly the same as {@link info.aduna.lang.FileFormat#matchMIMEType(String, Iterable)}
     * but only considers the <strong>default</strong> mimeTypes.
     *
     * @param mimeType A MIME type, e.g. "text/plain".
     * @param fileFormats The file formats to match the MIME type against.
     * @return A FileFormat object if the MIME type was recognized, or {@code null} otherwise.
     *
     * @see #matchDefaultMIMEType(String, Iterable, info.aduna.lang.FileFormat)
     * @see info.aduna.lang.FileFormat#matchMIMEType(String, Iterable)
     *
     */
    public static <FF extends FileFormat> FF matchDefaultMIMEType(String mimeType, Iterable<FF> fileFormats) {
        return matchDefaultMIMEType(mimeType, fileFormats, null);
    }

    /**
     * Tries to match the specified MIME type with the default MIME types of the supplied
     * file formats. The supplied fallback format will be returned when no
     * matching format was found.
     * This method does exactly the same as {@link info.aduna.lang.FileFormat#matchMIMEType(String, Iterable, info.aduna.lang.FileFormat)}
     * but only considers the <strong>default</strong> mimeTypes.
     *
     * @param mimeType A MIME type, e.g. "text/plain".
     * @param fileFormats The file formats to match the MIME type against.
     * @param fallback The file format to return if no matching format can be found.
     * @return A FileFormat that matches the MIME type, or the fallback format if the extension was not recognized.
     *
     * @see info.aduna.lang.FileFormat#matchMIMEType(String, Iterable, info.aduna.lang.FileFormat)
     *
     */
    public static <FF extends FileFormat> FF matchDefaultMIMEType(String mimeType, Iterable<FF> fileFormats,
                                                           FF fallback) {
        // Try to match with the default MIME type
        for (FF fileFormat : fileFormats) {
            if (fileFormat.hasDefaultMIMEType(mimeType)) {
                return fileFormat;
            }
        }
        return fallback;
    }

    public static String getContainer(String resource) throws MalformedURLException, URISyntaxException {
        final int fragmentIndex = resource.indexOf('#');
        if (fragmentIndex >= 0) {
            return resource.substring(0, fragmentIndex);
        }
        return resource.substring(0, resource.lastIndexOf('/', resource.length() - 1));
    }

    public static URI getContainer(URI resource) throws MalformedURLException, URISyntaxException {
        return new URIImpl(getContainer(resource.stringValue()));
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

    public static List<RDFFormat> filterAvailableParsers(List<RDFFormat> rdfFormats) {
        final List<RDFFormat> result = new ArrayList<>();
        final RDFParserRegistry parserRegistry = RDFParserRegistry.getInstance();
        for (RDFFormat f: rdfFormats) {
            if (parserRegistry.has(f)) {
                result.add(f);
            }
        }
        return result;
    }

    public static List<RDFFormat> filterAvailableWriters(List<RDFFormat> rdfFormats) {
        final List<RDFFormat> result = new ArrayList<>();
        final RDFWriterRegistry writerRegistry = RDFWriterRegistry.getInstance();
        for (RDFFormat f: rdfFormats) {
            if (writerRegistry.has(f)) {
                result.add(f);
            }
        }
        return result;
    }


    private LdpUtils() {
        // Static access only
    }

}
