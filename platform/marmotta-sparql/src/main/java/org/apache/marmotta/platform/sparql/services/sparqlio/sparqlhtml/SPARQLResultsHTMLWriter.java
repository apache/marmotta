/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
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
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class SPARQLResultsHTMLWriter implements TupleQueryResultWriter {

    private static final Logger log = LoggerFactory.getLogger(SPARQLResultsHTMLWriter.class);

    private OutputStream out;
    private ByteArrayOutputStream xmlOut;

    private SPARQLResultsXMLWriter xmlWriter;

    private Templates stylesheet;

    public SPARQLResultsHTMLWriter(OutputStream out) {
        this.out = out;
        this.xmlOut = new ByteArrayOutputStream();
        this.xmlWriter = new SPARQLResultsXMLWriter(xmlOut);
        Source s_stylesheet = new StreamSource(SPARQLResultsHTMLWriter.class.getResourceAsStream("style.xsl"));
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
        xmlWriter.startQueryResult(bindingNames);
    }

    /**
     * Indicates the end of a sequence of solutions.
     */
    @Override
    public void endQueryResult() throws TupleQueryResultHandlerException {
        xmlWriter.endQueryResult();

        // get server uri
        String server_uri = CDIContext.getInstance(ConfigurationService.class).getServerUri();

        byte[] queryResult = xmlOut.toByteArray();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        try {
            Source input      = new StreamSource(new ByteArrayInputStream(queryResult));

            Transformer transformer = stylesheet.newTransformer();
            transformer.setParameter("serverurl", server_uri);

            JDOMResult result = new JDOMResult();
            transformer.transform(input,result);
            Document output = result.getDocument();

            XMLOutputter printer = new XMLOutputter(Format.getPrettyFormat());
            printer.output(output, writer);
            writer.flush();

        } catch (Exception ex) {
            throw new TupleQueryResultHandlerException("error while transforming XML results to HTML",ex);
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
        xmlWriter.handleSolution(bindingSet);
    }
}
