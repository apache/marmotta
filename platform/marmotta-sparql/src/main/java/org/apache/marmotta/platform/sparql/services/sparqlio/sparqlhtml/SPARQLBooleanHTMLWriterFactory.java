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

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;

/**
 * A {@link BooleanQueryResultWriterFactory} for writers of SPARQL HTML query
 * boolean results.
 * 
 * @author Peter Ansell
 */
public class SPARQLBooleanHTMLWriterFactory implements BooleanQueryResultWriterFactory {

	/**
	 * Returns {@link SPARQLBooleanHTMLFormat#SPARQL_BOOLEAN_HTML}.
	 */
    @Override
	public BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return SPARQLBooleanHTMLFormat.SPARQL_BOOLEAN_HTML;
	}

	/**
	 * Returns a new instance of SPARQLBooleanHTMLWriter.
	 */
    @Override
	public BooleanQueryResultWriter getWriter(OutputStream out) {
		return new SPARQLBooleanHTMLWriter(out);
	}
}
