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
package org.apache.marmotta.ldclient.provider.freebase;

import com.google.common.base.Preconditions;

import javolution.util.function.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;

import com.github.vigsterkr.freebase.fix.FreebaseFixit;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Linked Data patched data provider to Freebase.
 * 
 * @author Sergio Fernández
 */
public class FreebaseProvider extends AbstractHttpProvider {

    public static final String NAME = "Freebase";
    public static final String PATTERN = "http(s?)://rdf\\.freebase\\.com/ns/.*";
    public static final String API = "https://www.googleapis.com/freebase/v1/rdf/";
    public static final RDFFormat DEFAULT_RDF_FORMAT = RDFFormat.TURTLE;
    public static final String DEFAULT_ENCODING = "UTF-8";
    private static final Pattern CHARSET_PATTERN = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");

    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     *
     * @return provider name
     */
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] listMimeTypes() {
        return new String[0];
    }

    /**
     * Build the URL for calling the API to retrieve the data for the resource passed.
     *
     * @param uri resource uri
     * @param endpoint endpoint
     * @return api url
     *
     * @see <a href="https://developers.google.com/freebase/v1/rdf">Freebase RDF Lookup</a>
     */
    @Override
    public List<String> buildRequestUrl(String uri, Endpoint endpoint) {
        Preconditions.checkState(StringUtils.isNotBlank(uri));
        String id = uri.substring(uri.lastIndexOf('/') + 1);
        String url = API + id.replace('.', '/');
        return Collections.singletonList(url);
    }

    @Override
    public List<String> parseResponse(final String resourceUri, final String requestUrl, Model triples, InputStream in, final String contentType) throws DataRetrievalException {

        RDFFormat format;
        if (StringUtils.isNotBlank(contentType) && (contentType.contains("text/plain")||contentType.contains("text/turtle"))) {
            format = DEFAULT_RDF_FORMAT;
        } else {
            format = Rio.getWriterFormatForMIMEType(contentType, DEFAULT_RDF_FORMAT);
        }

        try {
            if (DEFAULT_RDF_FORMAT.equals(format)) {
                String encoding;
                Matcher m = CHARSET_PATTERN.matcher(contentType);
                if (StringUtils.isNotBlank(contentType) && m.find()) {
                    encoding = m.group(1).trim().toUpperCase();
                } else {
                    encoding = DEFAULT_ENCODING;
                }
                in = fix(in, encoding);
            }
            ModelCommons.add(triples, in, resourceUri, format, new Predicate<Statement>() {
                @Override
                public boolean test(Statement param) {
                    return StringUtils.equals(param.getSubject().stringValue(), resourceUri);
                }
            });
            return Collections.emptyList();
        } catch (RDFParseException e) {
            throw new DataRetrievalException("parse error while trying to parse Turtle from Freebase", e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while trying to read remote Turtle from Freebase", e);
        }

    }

    /**
     * Fixes Freebase deficiencies on Turtle serialization
     *
     * @param is stream with the raw data
     * @return fixed stream
     */
    private InputStream fix(InputStream is, String encoding) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        PipedOutputStream po = new PipedOutputStream();
        PrintStream ps = new PrintStream(po);
        FreebaseFixit.fix(br, ps);
        ps.flush();
        ps.close();
        return new PipedInputStream(po);
    }

}
