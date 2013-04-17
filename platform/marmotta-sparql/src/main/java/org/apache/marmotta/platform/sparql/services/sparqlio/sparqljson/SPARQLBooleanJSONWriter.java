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
package org.apache.marmotta.platform.sparql.services.sparqlio.sparqljson;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class SPARQLBooleanJSONWriter implements BooleanQueryResultWriter {

    private OutputStream out;
    private WriterConfig config;

    public SPARQLBooleanJSONWriter(OutputStream out) {
        this.out = out;
    }

    /**
     * Gets the query result format that this writer uses.
     */
    @Override
    public BooleanQueryResultFormat getBooleanQueryResultFormat() {
        return new BooleanQueryResultFormat("SPARQL/JSON",
			"application/sparql-results+json", Charset.forName("UTF-8"), "srj");
    }

    /**
     * Writes the specified boolean value.
     */
    @Override
    public void write(boolean value) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        writer.println("{ \"head\": {}, \"boolean\": \""+value+"\" }");
        writer.flush();
        writer.close();
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

	@Override
	public QueryResultFormat getQueryResultFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleNamespace(String prefix, String uri) throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startDocument() throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleStylesheet(String stylesheetUrl) throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startHeader() throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endHeader() throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleBoolean(boolean arg0) throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLinks(List<String> arg0) throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleSolution(BindingSet arg0) throws TupleQueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startQueryResult(List<String> arg0) throws TupleQueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}    
}
