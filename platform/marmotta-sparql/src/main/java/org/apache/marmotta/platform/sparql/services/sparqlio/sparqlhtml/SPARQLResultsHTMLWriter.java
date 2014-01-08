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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL results to HTML writer using Freemarker
 * 
 * @author Sergio Fernández
 */
public class SPARQLResultsHTMLWriter extends TupleQueryResultHandlerBase implements TupleQueryResultWriter {

	private static final Logger log = LoggerFactory.getLogger(SPARQLResultsHTMLWriter.class);
    
    private static final String START_TEMPLATE = "sparql_select_start.ftl";

	private static final String RESULT_TEMPLATE = "sparql_select_result.ftl";

	private static final String END_TEMPLATE = "sparql_select_end.ftl";

	private static final Collection<RioSetting<?>> SUPPORTED_SETTINGS;

	static
	{
	    HashSet<RioSetting<?>> tempSettings = new HashSet<RioSetting<?>>();
	    tempSettings.add(SPARQLHTMLSettings.TEMPLATING_SERVICE);
	    SUPPORTED_SETTINGS = Collections.unmodifiableSet(tempSettings);
	}
	
    final private OutputStream out;
    
    private List<String> vars;
    
    private TemplatingService templatingService;
    
    private WriterConfig config;
    
    public SPARQLResultsHTMLWriter(OutputStream out) {
        this.out = out;
        this.config = new WriterConfig(); 
    }
    
    public SPARQLResultsHTMLWriter(OutputStream out, TemplatingService templatingService) {
        this(out);
        this.templatingService = templatingService;
    }
    
    @Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return SPARQLResultsHTMLFormat.SPARQL_RESULTS_HTML;
	}

	@Override
	public void startQueryResult(List<String> vars) throws TupleQueryResultHandlerException {
	    if(templatingService == null) {
	        templatingService = getWriterConfig().get(SPARQLHTMLSettings.TEMPLATING_SERVICE);
	        if(templatingService == null) {
	            throw new IllegalStateException("Templating service was not setup");
	        }
	    }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("vars", vars);
        this.vars = vars;
        try {
            templatingService.process(SPARQLResultsHTMLWriter.class, START_TEMPLATE, data, new OutputStreamWriter(out));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new TupleQueryResultHandlerException(e);
        }
	}
	
	@Override
	public void handleSolution(BindingSet binding) throws TupleQueryResultHandlerException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("vars", vars);
        Map<String, String> result = new HashMap<String, String>();
        for (String var: vars) {
        	if (binding.hasBinding(var)) {
        		result.put(var, binding.getBinding(var).getValue().stringValue());
        	} else {
        		result.put(var, "");
        	}
        }
        data.put("result", result);
        try {
            templatingService.process(SPARQLResultsHTMLWriter.class, RESULT_TEMPLATE, data, new OutputStreamWriter(out));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new TupleQueryResultHandlerException(e);
        }
	}	
	
	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {
		Map<String, Object> data = new HashMap<String, Object>();
        try {            
            templatingService.process(SPARQLResultsHTMLWriter.class, END_TEMPLATE, data, new OutputStreamWriter(out));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new TupleQueryResultHandlerException(e);
        }
	}

	@Override
	public void handleLinks(List<String> arg0)
			throws QueryResultHandlerException {
		// TODO Auto-generated method stub
	}

	@Override
	public QueryResultFormat getQueryResultFormat() {
	    return getTupleQueryResultFormat();
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
		return SUPPORTED_SETTINGS;
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
