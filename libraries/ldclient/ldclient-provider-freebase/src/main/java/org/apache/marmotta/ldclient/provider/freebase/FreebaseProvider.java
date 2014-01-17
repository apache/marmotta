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
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Linked Data patched data provider to Freebase.
 * 
 * @author Sergio Fernández
 */
public class FreebaseProvider extends AbstractHttpProvider {

    public static final String PROVIDER_NAME = "Freebase";
    public static final String API = "https://www.googleapis.com/freebase/v1/rdf/";

    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     *
     * @return provider name
     */
    @Override
    public String getName() {
        return PROVIDER_NAME;
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
        Preconditions.checkNotNull(uri);
        String id = uri.substring(uri.lastIndexOf('/') + 1);
        String url = API + id.replace('.', '/');
        return Collections.singletonList(url);
    }

    @Override
    public List<String> parseResponse(final String resourceUri, final String requestUrl, Model triples, InputStream in, final String contentType) throws DataRetrievalException {
        Preconditions.checkState(contentType.contains("text/plain"), "Unexpected content type: " + contentType);
        RDFFormat format = RDFFormat.TURTLE;
        try {
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

}
