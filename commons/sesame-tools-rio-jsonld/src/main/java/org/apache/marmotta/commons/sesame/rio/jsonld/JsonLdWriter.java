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
package org.apache.marmotta.commons.sesame.rio.jsonld;

import de.dfki.km.json.jsonld.impl.SesameJSONLDSerializer;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class JsonLdWriter implements RDFWriter {

    private PrintWriter writer;
    private WriterConfig config;
    
    SesameJSONLDSerializer serializer;

    public JsonLdWriter(OutputStream out) {
        writer = new PrintWriter(new OutputStreamWriter(out));
        serializer = new SesameJSONLDSerializer();
    }


    public JsonLdWriter(Writer writer) {
        this.writer = new PrintWriter(writer);
        serializer = new SesameJSONLDSerializer();
    }


    /**
     * Gets the RDF format that this RDFWriter uses.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.JSONLD;
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
    }

    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        writer.print(serializer.asString());
        writer.flush();
    }

    /**
     * Handles a namespace declaration/definition. A namespace declaration
     * associates a (short) prefix string with the namespace's URI. The prefix
     * for default namespaces, which do not have an associated prefix, are
     * represented as empty strings.
     *
     * @param prefix The prefix for the namespace, or an empty string in case of a
     *               default namespace.
     * @param uri    The URI that the prefix maps to.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        serializer.setPrefix(uri,prefix);
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        serializer.handleStatement(st);
    }

    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {
    	
    }
    
    /**
     * @return A collection of {@link RioSetting}s that are supported by this
     *         RDFWriter.
     * @since 2.7.0
     */
	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		return new ArrayList<RioSetting<?>>();
	}

    /**
     * Retrieves the current writer configuration as a single object.
     * 
     * @return a writer configuration object representing the current
     *         configuration of the writer.
     * @since 2.7.0
     */
	@Override
	public WriterConfig getWriterConfig() {
		return config;
	}

    /**
     * Sets all supplied writer configuration options.
     * 
     * @param config
     *        a writer configuration object.
     * @since 2.7.0
     */
	@Override
	public void setWriterConfig(WriterConfig config) {
		this.config = config;
	}    
}
