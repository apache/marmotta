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
package org.apache.marmotta.ldclient.provider.rdf;

import javolution.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.sesame.model.ModelCommons;
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
 * Abstract implementation for RDF-aware data providers.
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class AbstractRDFProvider extends AbstractHttpProvider {

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[] {
                "application/rdf+xml",
                "text/rdf+n3; q=0.8",
                "text/turtle; q=0.6"
        };
    }

    /**
     * Parse the HTTP response entity returned by the web service call and return its contents as a Sesame RDF
     * repository. The content type returned by the web service is passed as argument to help the implementation
     * decide how to parse the data.
     *
     *
     * @param resourceUri
     * @param triples
     *@param in input stream as returned by the remote webservice
     * @param contentType content type as returned in the HTTP headers of the remote webservice   @return an RDF repository containing an RDF representation of the dataset located at the remote resource.
     * @throws java.io.IOException in case an error occurs while reading the input stream
     */
    @Override
    public List<String> parseResponse(final String resourceUri, String requestUrl, Model triples, InputStream in, String contentType) throws DataRetrievalException {
        RDFFormat format = RDFParserRegistry.getInstance().getFileFormatForMIMEType(contentType, RDFFormat.RDFXML);

        try {
            ModelCommons.add(triples, in, resourceUri, format, new Predicate<Statement>() {
                @Override
                public boolean test(Statement param) {
                    return StringUtils.equals(param.getSubject().stringValue(), resourceUri);
                }
            });

            return Collections.emptyList();
        } catch (RDFParseException e) {
            throw new DataRetrievalException("parse error while trying to parse remote RDF content",e);
        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while trying to read remote RDF content",e);
        }
    }

}
