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
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

import javax.ws.rs.core.MediaType;

/**
 * Various Util-Methods for the {@link org.apache.marmotta.platform.ldp.api.LdpService}.
 */
public class LdpUtils {

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
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        try {
            return allTypes.forName(mimeType).getExtension();
        } catch (MimeTypeException e) {
            return null; //FIXME
        }
    }

    /**
     * Get <b>only</b> the mimeType from the {@link javax.ws.rs.core.MediaType}
     *
     * @param mediaType the mediaType
     * @return the mimeType
     */
    public static String getMimeType(MediaType mediaType) {
        return String.format("%s/%s", mediaType.getType(), mediaType.getSubtype());
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

        writer.handleNamespace("", subject.stringValue());

        while (iteration.hasNext()) {
            writer.handleStatement(iteration.next());
        }

        writer.endRDF();
    }

}
