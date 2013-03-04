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
package org.apache.marmotta.platform.core.api.exporter;

import org.apache.marmotta.platform.core.exception.io.UnsupportedExporterException;
import org.openrdf.model.URI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Set;

/**
 * Methods for writing triple data into different targets using different exporters.
 *
 * @author Sebastian Schaffert
 *
 */
public interface ExportService {

    /**
     * Get a collection of all mime types accepted by this exporter. Used for automatically
     * selecting the appropriate exporter in ExportService.
     *
     * @return a set of strings representing the mime types accepted by this exporter
     */
    public Set<String> getProducedTypes();


    /**
     * Export the triple data contained in the named graph passed as argument "context" and return
     * it as a Java string using the serialisation format specified by "mimeType".
     * <p/>
     * The context parameter limits the exported triples to the named graph represented by this
     * resource. If it is set to null, all named graphs will be exported.
     * <p/>
     * The mime type must be supported by at least one of the registered exporters, otherwise an
     * UnsupportedExporterException. Available mime types can be retrieved using the getProducedTypes()
     * method.
     *
     *
     * @param context  the named graph to export; if null, all named graphs will be exported
     * @param mimeType a mime type registered by an exporter
     *
     * @throws UnsupportedExporterException in case there is no matching exporter for the given mime type
     */
    public String exportData(URI context, String mimeType) throws UnsupportedExporterException;

    /**
     * Export the triple data contained in the named graph passed as argument "context" and write it
     * to the writer given as first argument using the serialisation format specified by "mimeType".
     * <p/>
     * The context parameter limits the exported triples to the named graph represented by this
     * resource. If it is set to null, all named graphs will be exported.
     * <p/>
     * The mime type must be supported by at least one of the registered exporters, otherwise an
     * UnsupportedExporterException. Available mime types can be retrieved using the getProducedTypes()
     * method.
     *
     *
     * @param writer   the writer to write the triples to; will be closed when the triples are written
     * @param context  the named graph to export; if null, all named graphs will be exported
     * @param mimeType a mime type registered by an exporter
     *
     * @throws UnsupportedExporterException in case there is no matching exporter for the given mime type
     * @throws IOException in case there is an error writing to the output
     */
    public void exportData(Writer writer, URI context, String mimeType) throws UnsupportedExporterException, IOException;


    /**
     * Export the triple data contained in the named graph passed as argument "context" and write it
     * to the output stream given as first argument using the serialisation format specified by "mimeType".
     * <p/>
     * The context parameter limits the exported triples to the named graph represented by this
     * resource. If it is set to null, all named graphs will be exported.
     * <p/>
     * The mime type must be supported by at least one of the registered exporters, otherwise an
     * UnsupportedExporterException. Available mime types can be retrieved using the getProducedTypes()
     * method.
     *
     *
     * @param outputStream   the OutputStream to write the triples to; data will be written using UTF-8 encoding;
     *                       will be closed when the triples are written
     * @param context  the named graph to export; if null, all named graphs will be exported
     * @param mimeType a mime type registered by an exporter
     *
     * @throws UnsupportedExporterException in case there is no matching exporter for the given mime type
     * @throws IOException in case there is an error writing to the output
     */
    public void exportData(OutputStream outputStream, URI context, String mimeType) throws UnsupportedExporterException, IOException;



    /**
     * Export the triple data for the given resource contained in the named graph passed as argument "context" and return
     * it as a Java string using the serialisation format specified by "mimeType".
     * <p/>
     * The context parameter limits the exported triples to the named graph represented by this
     * resource. If it is set to null, all named graphs will be exported.
     * <p/>
     * The mime type must be supported by at least one of the registered exporters, otherwise an
     * UnsupportedExporterException. Available mime types can be retrieved using the getProducedTypes()
     * method.
     *
     *
     * @param context  the named graph to export; if null, all named graphs will be exported
     * @param mimeType a mime type registered by an exporter
     *
     * @throws UnsupportedExporterException in case there is no matching exporter for the given mime type
     */
    public String exportData(URI resource, URI context, String mimeType) throws UnsupportedExporterException;

    /**
     * Export the triple data for the given resource contained in the named graph passed as argument "context" and write it
     * to the writer given as first argument using the serialisation format specified by "mimeType".
     * <p/>
     * The context parameter limits the exported triples to the named graph represented by this
     * resource. If it is set to null, all named graphs will be exported.
     * <p/>
     * The mime type must be supported by at least one of the registered exporters, otherwise an
     * UnsupportedExporterException. Available mime types can be retrieved using the getProducedTypes()
     * method.
     *
     *
     * @param writer   the writer to write the triples to; will be closed when the triples are written
     * @param context  the named graph to export; if null, all named graphs will be exported
     * @param mimeType a mime type registered by an exporter
     *
     * @throws UnsupportedExporterException in case there is no matching exporter for the given mime type
     * @throws IOException in case there is an error writing to the output
     */
    public void exportData(Writer writer, URI resource, URI context, String mimeType) throws UnsupportedExporterException, IOException;


    /**
     * Export the triple data for the given resource contained in the named graph passed as argument "context" and write it
     * to the output stream given as first argument using the serialisation format specified by "mimeType".
     * <p/>
     * The context parameter limits the exported triples to the named graph represented by this
     * resource. If it is set to null, all named graphs will be exported.
     * <p/>
     * The mime type must be supported by at least one of the registered exporters, otherwise an
     * UnsupportedExporterException. Available mime types can be retrieved using the getProducedTypes()
     * method.
     *
     *
     * @param outputStream   the OutputStream to write the triples to; data will be written using UTF-8 encoding;
     *                       will be closed when the triples are written
     * @param context  the named graph to export; if null, all named graphs will be exported
     * @param mimeType a mime type registered by an exporter
     *
     * @throws UnsupportedExporterException in case there is no matching exporter for the given mime type
     * @throws IOException in case there is an error writing to the output
     */
    public void exportData(OutputStream outputStream, URI resource, URI context, String mimeType) throws UnsupportedExporterException, IOException;

}
