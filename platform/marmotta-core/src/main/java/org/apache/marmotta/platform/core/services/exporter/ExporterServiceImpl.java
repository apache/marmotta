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
package org.apache.marmotta.platform.core.services.exporter;

import com.google.common.collect.ImmutableSet;
import org.apache.marmotta.platform.core.api.exporter.ExportService;
import org.apache.marmotta.platform.core.api.io.MarmottaIOService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.io.UnsupportedExporterException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

/**
 * Methods for writing triple data into different targets using different exporters.
 *
 * @author Sebastian Schaffert
 *
 */
@ApplicationScoped
public class ExporterServiceImpl implements ExportService {

    @Inject
    private Logger log;

    @Inject
    private MarmottaIOService ioService;


    @Inject
    private SesameService sesameService;



    /**
     * Get a collection of all mime types accepted by this exporter. Used for automatically
     * selecting the appropriate exporter in ExportService.
     *
     * @return a set of strings representing the mime types accepted by this exporter
     */
    @Override
    public Set<String> getProducedTypes() {
        return ImmutableSet.copyOf(ioService.getProducedTypes());
    }

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
     * @throws org.apache.marmotta.platform.core.exception.io.UnsupportedExporterException
     *                             in case there is no matching exporter for the given mime type
     */
    @Override
    public String exportData(URI context, String mimeType) throws UnsupportedExporterException {
        RDFFormat serializer = ioService.getSerializer(mimeType);
        if(serializer == null) {
            log.warn("could not find serializer for MIME type {}",mimeType);
            throw new UnsupportedExporterException("No serializer for mime type "+mimeType);
        }
        try {
            StringWriter writer = new StringWriter();
            exportData(writer,context,mimeType);
            return writer.toString();
        } catch (IOException e) {
            log.error("impossible I/O error while writing to string buffer",e);
            return null;
        }
    }

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
     * @throws org.apache.marmotta.platform.core.exception.io.UnsupportedExporterException
     *                             in case there is no matching exporter for the given mime type
     * @throws java.io.IOException in case there is an error writing to the output
     */
    @Override
    public void exportData(Writer writer, URI context, String mimeType) throws UnsupportedExporterException, IOException {
        RDFFormat serializer = ioService.getSerializer(mimeType);
        if(serializer == null) {
            log.warn("could not find serializer for MIME type {}",mimeType);
            throw new UnsupportedExporterException("No serializer for mime type "+mimeType);
        }

        // HINT: This method might be executed outside a transaction!
        RDFWriter handler = Rio.createWriter(serializer,writer);
        try {
            RepositoryConnection connection = sesameService.getConnection();
            connection.begin();
            try {
                if(context == null) {
                    connection.exportStatements(null,null,null,true,handler);
                } else {
                    connection.exportStatements(null,null,null,true,handler,context);
                }
            } finally {
                connection.commit();
                connection.close();
            }
        } catch (RepositoryException e) {
            throw new IOException("error while getting repository connection");
        } catch (RDFHandlerException e) {
            throw new IOException("error while writing RDF data to stream");
        }
    }

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
     * @param outputStream the OutputStream to write the triples to; data will be written using UTF-8 encoding;
     *                     will be closed when the triples are written
     * @param context      the named graph to export; if null, all named graphs will be exported
     * @param mimeType     a mime type registered by an exporter
     * @throws org.apache.marmotta.platform.core.exception.io.UnsupportedExporterException
     *                             in case there is no matching exporter for the given mime type
     * @throws java.io.IOException in case there is an error writing to the output
     */
    @Override
    public void exportData(OutputStream outputStream, URI context, String mimeType) throws UnsupportedExporterException, IOException {
        RDFFormat serializer = ioService.getSerializer(mimeType);
        if(serializer == null) {
            log.warn("could not find serializer for MIME type {}",mimeType);
            throw new UnsupportedExporterException("No serializer for mime type "+mimeType);
        }
        // HINT: This method might be executed outside a transaction!
        RDFWriter handler = Rio.createWriter(serializer,outputStream);
        try {
            RepositoryConnection connection = sesameService.getConnection();
            try {
                if(context == null) {
                    connection.exportStatements(null,null,null,true,handler);
                } else {
                    connection.exportStatements(null,null,null,true,handler,context);
                }
            } finally {
                connection.commit();
                connection.close();
            }
        } catch (RepositoryException e) {
            throw new IOException("error while getting repository connection");
        } catch (RDFHandlerException e) {
            throw new IOException("error while writing RDF data to stream");
        }
    }


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
     * @param context  the named graph to export; if null, all named graphs will be exported
     * @param mimeType a mime type registered by an exporter
     * @throws org.apache.marmotta.platform.core.exception.io.UnsupportedExporterException
     *          in case there is no matching exporter for the given mime type
     */
    @Override
    public String exportData(URI resource, URI context, String mimeType) throws UnsupportedExporterException {
        RDFFormat serializer = ioService.getSerializer(mimeType);
        if(serializer == null) {
            log.warn("could not find serializer for MIME type {}",mimeType);
            throw new UnsupportedExporterException("No serializer for mime type "+mimeType);
        }
        try {
            StringWriter writer = new StringWriter();
            exportData(writer,resource,context,mimeType);
            return writer.toString();
        } catch (IOException e) {
            log.error("impossible I/O error while writing to string buffer",e);
            return null;
        }
    }

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
     * @throws org.apache.marmotta.platform.core.exception.io.UnsupportedExporterException
     *                             in case there is no matching exporter for the given mime type
     * @throws java.io.IOException in case there is an error writing to the output
     */
    @Override
    public void exportData(Writer writer, URI resource, URI context, String mimeType) throws UnsupportedExporterException, IOException {
        RDFFormat serializer = ioService.getSerializer(mimeType);
        if(serializer == null) {
            log.warn("could not find serializer for MIME type {}",mimeType);
            throw new UnsupportedExporterException("No serializer for mime type "+mimeType);
        }

        // HINT: This method might be executed outside a transaction!
        RDFWriter handler = Rio.createWriter(serializer,writer);
        try {
            RepositoryConnection connection = sesameService.getConnection();
            try {
                if(context == null) {
                    connection.exportStatements(resource,null,null,true,handler);
                } else {
                    connection.exportStatements(resource,null,null,true,handler,context);
                }
            } finally {
                connection.commit();
                connection.close();
            }
        } catch (RepositoryException e) {
            throw new IOException("error while getting repository connection");
        } catch (RDFHandlerException e) {
            throw new IOException("error while writing RDF data to stream");
        }
    }

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
     * @param outputStream the OutputStream to write the triples to; data will be written using UTF-8 encoding;
     *                     will be closed when the triples are written
     * @param context      the named graph to export; if null, all named graphs will be exported
     * @param mimeType     a mime type registered by an exporter
     * @throws org.apache.marmotta.platform.core.exception.io.UnsupportedExporterException
     *                             in case there is no matching exporter for the given mime type
     * @throws java.io.IOException in case there is an error writing to the output
     */
    @Override
    public void exportData(OutputStream outputStream, URI resource, URI context, String mimeType) throws UnsupportedExporterException, IOException {
        RDFFormat serializer = ioService.getSerializer(mimeType);
        if(serializer == null) {
            log.warn("could not find serializer for MIME type {}",mimeType);
            throw new UnsupportedExporterException("No serializer for mime type "+mimeType);
        }
        // HINT: This method might be executed outside a transaction!
        RDFWriter handler = Rio.createWriter(serializer,outputStream);
        try {
            RepositoryConnection connection = sesameService.getConnection();
            try {
                if(context == null) {
                    connection.exportStatements(resource,null,null,true,handler);
                } else {
                    connection.exportStatements(resource,null,null,true,handler,context);
                }
            } finally {
                connection.commit();
                connection.close();
            }
        } catch (RepositoryException e) {
            throw new IOException("error while getting repository connection");
        } catch (RDFHandlerException e) {
            throw new IOException("error while writing RDF data to stream");
        }
    }
}
