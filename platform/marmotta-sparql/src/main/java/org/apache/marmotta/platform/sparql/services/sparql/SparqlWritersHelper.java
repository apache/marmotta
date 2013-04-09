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
package org.apache.marmotta.platform.sparql.services.sparql;

import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.sparql.services.sparqlio.rdf.SPARQLGraphResultWriter;
import org.apache.marmotta.platform.sparql.services.sparqlio.sparqlhtml.SPARQLBooleanHTMLWriter;
import org.apache.marmotta.platform.sparql.services.sparqlio.sparqlhtml.SPARQLResultsHTMLWriter;
import org.apache.marmotta.platform.sparql.services.sparqlio.sparqljson.SPARQLBooleanJSONWriter;
import org.apache.marmotta.platform.sparql.services.sparqlio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLBooleanXMLWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.query.resultio.text.BooleanTextWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;

/**
 * Helper for working with SPARQL Writers
 * 
 * @author Sergio Fernández
 *
 */
public class SparqlWritersHelper {
	
    public static TupleQueryResultWriter getTupleResultWriter(String format, OutputStream os) {
        //build outputwriter
        final TupleQueryResultWriter out;
        if(format == null) {
            out = new SPARQLResultsXMLWriter(os);
        } else if(parseSubType(format).equals("html")) {
            out = new SPARQLResultsHTMLWriter(os);
        } else if(parseSubType(format).equals("json")) {
            out = new SPARQLResultsJSONWriter(os);
        } else if(parseSubType(format).equals("xml")) {
            out = new SPARQLResultsXMLWriter(os);
        } else if(parseSubType(format).equals("csv")) {
            out = new SPARQLResultsCSVWriter(os);
        } else throw new InvalidArgumentException("could not produce format "+format);
        return out;
    }

    public static BooleanQueryResultWriter getBooleanResultWriter(String format, OutputStream os) {
        //build outputwriter
        final BooleanQueryResultWriter out;
        if(format == null) {
            out = new SPARQLBooleanXMLWriter(os);
        } else if(parseSubType(format).equals("html")) {
            out = new SPARQLBooleanHTMLWriter(os);
        } else if(parseSubType(format).equals("json")) {
            out = new SPARQLBooleanJSONWriter(os);
        } else if(parseSubType(format).equals("xml")) {
            out = new SPARQLBooleanXMLWriter(os);
        } else if(parseSubType(format).equals("csv")) {
            out = new BooleanTextWriter(os);
        } else throw new InvalidArgumentException("could not produce format "+format);
        return out;
    }

    public static SPARQLGraphResultWriter getGraphResultWriter(String format, OutputStream os) {
        return new SPARQLGraphResultWriter(os,format);
    }
    
    //TODO: move this to another place
    
    public static Pattern subTypePattern = Pattern.compile("[a-z]+/([a-z0-9-._]+\\+)?([a-z0-9-._]+)(;.*)?");
    
    public static String parseSubType(String mimeType) {
        Matcher matcher = subTypePattern.matcher(mimeType);
        if (matcher.matches())
            return matcher.group(2);
        else
            return mimeType;
    }
    
}
