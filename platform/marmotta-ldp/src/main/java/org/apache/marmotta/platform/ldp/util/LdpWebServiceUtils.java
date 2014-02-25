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
import info.aduna.iteration.Iteration;
import info.aduna.iteration.UnionIteration;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * Various Util-Methods for the {@link org.apache.marmotta.platform.ldp.webservices.LdpWebService}.
 */
public class LdpWebServiceUtils {

    /**
     * Urify the Slug: header value, i.e. replace all non-url chars with a single dash.
     *
     * @param slugHeaderValue the client-provided Slug-header
     * @return the slugHeaderValue "urified"
     */
    public static String urify(String slugHeaderValue) {
        return slugHeaderValue
                // Replace non-url chars with '-'
                .replaceAll("[^\\w]+", "-");
    }

    /**
     * LDP-Style to serialize a resource.
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
