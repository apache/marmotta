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

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class SPARQLBooleanJSONWriter implements BooleanQueryResultWriter {


    private OutputStream out;

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
}
