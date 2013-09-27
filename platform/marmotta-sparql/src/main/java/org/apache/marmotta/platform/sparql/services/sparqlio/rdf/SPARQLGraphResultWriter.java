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
package org.apache.marmotta.platform.sparql.services.sparqlio.rdf;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultWriter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * SPARQL graph result writer for Sesame RIO
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 * @deprecated Only the getFormat and getOutputStream methods are used. Use alternative non-deprecated API methods to avoid using this class.
 */
@Deprecated
public class SPARQLGraphResultWriter implements QueryResultWriter {

    private OutputStream outputStream;

    private RDFFormat format;
    
    private WriterConfig config;

    public SPARQLGraphResultWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
        format = RDFFormat.RDFXML;
    }

    public SPARQLGraphResultWriter(OutputStream outputStream, String mimeType) {
        this.outputStream = outputStream;
        this.format = Rio.getWriterFormatForMIMEType(mimeType, RDFFormat.RDFXML);
    }

    public RDFFormat getFormat() {
        return format;
    }
    
    public OutputStream getOutputStream() {
        return outputStream;
    }
    
    @Deprecated
    public void write(GraphQueryResult result) throws IOException {
        try {
            QueryResultIO.write(result, format, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (QueryEvaluationException e) {
            throw new IOException("query result writing failed because query evaluation had a problem", e);
        } catch (RDFHandlerException e) {
            throw new IOException("query result writing failed because writer could not handle rdf data", e);
        }
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

	@Override
	public QueryResultFormat getQueryResultFormat() {
		return new QueryResultFormat("QueryResultFormat", format.getDefaultMIMEType(), Charset.defaultCharset(), format.getDefaultFileExtension());
	}

	@Override
	public void handleNamespace(String prefix, String uri) throws QueryResultHandlerException {
		
	}

	@Override
	public void startDocument() throws QueryResultHandlerException {
		
	}

	@Override
	public void handleStylesheet(String stylesheetUrl) throws QueryResultHandlerException {
		
	}

	@Override
	public void startHeader() throws QueryResultHandlerException {
		
	}

	@Override
	public void endHeader() throws QueryResultHandlerException {
		
	}

	@Override
	public void setWriterConfig(WriterConfig config) {
		this.config = config;
	}

	@Override
	public WriterConfig getWriterConfig() {
		return config;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<RioSetting<?>> getSupportedSettings() {
		return Collections.emptyList();
	}
    
}
