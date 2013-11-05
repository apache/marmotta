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
package org.apache.marmotta.platform.sparql.services.sparqlio.sparqlhtml;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.util.CDIContext;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ASPARQL Results to HTMl writer using XSL
 * 
 * @author sschaffe
 */
@Deprecated
public class SPARQLResultsHTMLWriterXSL implements TupleQueryResultWriter {

    private static final Logger log = LoggerFactory.getLogger(SPARQLResultsHTMLWriterXSL.class);

    private OutputStream out;
    private ByteArrayOutputStream xmlOut;

    private SPARQLResultsXMLWriter writer;
    
    private WriterConfig config;

    private Templates stylesheet;

    public SPARQLResultsHTMLWriterXSL(OutputStream out) {
        this.out = out;
        this.xmlOut = new ByteArrayOutputStream();
        this.writer = new SPARQLResultsXMLWriter(xmlOut);
        Source s_stylesheet = new StreamSource(SPARQLResultsHTMLWriterXSL.class.getResourceAsStream("style.xsl"));
        try {
            stylesheet = TransformerFactory.newInstance().newTemplates(s_stylesheet);
        } catch (TransformerConfigurationException e) {
            log.error("could not compile stylesheet for rendering SPARQL results; result display not available!");
        }
    }

    /**
     * Gets the query result format that this writer uses.
     */
    @Override
    public TupleQueryResultFormat getTupleQueryResultFormat() {
        return new TupleQueryResultFormat("SPARQL/HTML","text/html", Charset.forName("UTF-8"), "html");
    }

    /**
     * Indicates the start of a sequence of Solutions. The supplied bindingNames
     * are an indication of the values that are in the Solutions. For example, a
     * SeRQL query like <tt>select X, Y from {X} P {Y} </tt> will have binding
     * names <tt>X</tt> and <tt>Y</tt>.
     *
     * @param bindingNames An ordered set of binding names.
     */
    @Override
    public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
        writer.startQueryResult(bindingNames);
    }

    /**
     * Indicates the end of a sequence of solutions.
     */
    @Override
    public void endQueryResult() throws TupleQueryResultHandlerException {
        writer.endQueryResult();

        // get server uri
        String server_uri = CDIContext.getInstance(ConfigurationService.class).getServerUri();

        byte[] queryResult = xmlOut.toByteArray();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        try {
            Source input      = new StreamSource(new ByteArrayInputStream(queryResult));

            Transformer transformer = stylesheet.newTransformer();
            transformer.setParameter("serverurl", server_uri);

            JDOMResult result = new JDOMResult();
            transformer.transform(input, result);
            Document output = result.getDocument();

            XMLOutputter printer = new XMLOutputter(Format.getPrettyFormat());
            printer.output(output, writer);
            writer.flush();

        } catch (Exception ex) {
            throw new TupleQueryResultHandlerException("error while transforming XML results to HTML", ex);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {}
        }

    }

    /**
     * Handles a solution.
     */
    @Override
    public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
        writer.handleSolution(bindingSet);
    }

	@Override
	public void handleBoolean(boolean arg0) throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleLinks(List<String> arg0)
			throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public QueryResultFormat getQueryResultFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleNamespace(String prefix, String uri)
			throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startDocument() throws QueryResultHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleStylesheet(String stylesheetUrl)
			throws QueryResultHandlerException {
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
