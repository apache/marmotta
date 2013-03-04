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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import info.aduna.io.IndentingWriter;
import info.aduna.text.StringUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * A TupleQueryResultWriter that writes query results in the <a
 * href="http://www.w3.org/TR/rdf-sparql-json-res/">SPARQL Query Results JSON
 * Format</a>.
 */
public class SPARQLResultsJSONWriter implements TupleQueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

    private IndentingWriter writer;

    private boolean firstTupleWritten;

	/*--------------*
	 * Constructors *
	 *--------------*/

    public SPARQLResultsJSONWriter(OutputStream out) {
        Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        w = new BufferedWriter(w, 1024);
        writer = new IndentingWriter(w);
    }

	/*---------*
	 * Methods *
	 *---------*/

    public final TupleQueryResultFormat getTupleQueryResultFormat() {
        return TupleQueryResultFormat.JSON;
    }

    public void startQueryResult(List<String> columnHeaders)
            throws TupleQueryResultHandlerException
    {
        try {
            openBraces();

            // Write header
            writeKey("head");
            openBraces();
            writeKeyValue("vars", columnHeaders);
            closeBraces();

            writeComma();

            // Write results
            writeKey("results");
            openBraces();

            writeKey("bindings");
            openArray();

            firstTupleWritten = false;
        }
        catch (IOException e) {
            throw new TupleQueryResultHandlerException(e);
        }
    }

    public void endQueryResult()
            throws TupleQueryResultHandlerException
    {
        try {
            closeArray(); // bindings array
            closeBraces(); // results braces
            closeBraces(); // root braces
            writer.flush();
        }
        catch (IOException e) {
            throw new TupleQueryResultHandlerException(e);
        }
    }

    public void handleSolution(BindingSet bindingSet)
            throws TupleQueryResultHandlerException
    {
        try {
            if (firstTupleWritten) {
                writeComma();
            }
            else {
                firstTupleWritten = true;
            }

            openBraces(); // start of new solution

            Iterator<Binding> bindingIter = bindingSet.iterator();
            while (bindingIter.hasNext()) {
                Binding binding = bindingIter.next();

                writeKeyValue(binding.getName(), binding.getValue());

                if (bindingIter.hasNext()) {
                    writeComma();
                }
            }

            closeBraces(); // end solution

            writer.flush();
        }
        catch (IOException e) {
            throw new TupleQueryResultHandlerException(e);
        }
    }

    private void writeKeyValue(String key, String value)
            throws IOException
    {
        writeKey(key);
        writeString(value);
    }

    private void writeKeyValue(String key, Value value)
            throws IOException, TupleQueryResultHandlerException
    {
        writeKey(key);
        writeValue(value);
    }

    private void writeKeyValue(String key, Iterable<String> array)
            throws IOException
    {
        writeKey(key);
        writeArray(array);
    }

    private void writeKey(String key)
            throws IOException
    {
        writeString(key);
        writer.write(": ");
    }

    private void writeValue(Value value)
            throws IOException, TupleQueryResultHandlerException
    {
        writer.write("{ ");

        if (value instanceof URI) {
            writeKeyValue("type", "uri");
            writer.write(", ");
            writeKeyValue("value", ((URI)value).toString());
        }
        else if (value instanceof BNode) {
            writeKeyValue("type", "bnode");
            writer.write(", ");
            writeKeyValue("value", ((BNode)value).getID());
        }
        else if (value instanceof Literal) {
            Literal lit = (Literal)value;

            if (lit.getLanguage() != null) {
                writeKeyValue("xml:lang", lit.getLanguage());
                writer.write(", ");
            }
            if (lit.getDatatype() != null) {
                writeKeyValue("datatype", lit.getDatatype().toString());
                writer.write(", ");
            }

            writeKeyValue("type", "literal");

            writer.write(", ");
            writeKeyValue("value", lit.getLabel());
        }
        else {
            throw new TupleQueryResultHandlerException("Unknown Value object type: " + value.getClass());
        }

        writer.write(" }");
    }

    private void writeString(String value)
            throws IOException
    {
        // Escape special characters
        value = StringUtil.gsub("\\", "\\\\", value);
        value = StringUtil.gsub("\"", "\\\"", value);
        value = StringUtil.gsub("/", "\\/", value);
        value = StringUtil.gsub("\b", "\\b", value);
        value = StringUtil.gsub("\f", "\\f", value);
        value = StringUtil.gsub("\n", "\\n", value);
        value = StringUtil.gsub("\r", "\\r", value);
        value = StringUtil.gsub("\t", "\\t", value);

        writer.write("\"");
        writer.write(value);
        writer.write("\"");
    }

    private void writeArray(Iterable<String> array)
            throws IOException
    {
        writer.write("[ ");

        Iterator<String> iter = array.iterator();
        while (iter.hasNext()) {
            String value = iter.next();

            writeString(value);

            if (iter.hasNext()) {
                writer.write(", ");
            }
        }

        writer.write(" ]");
    }

    private void openArray()
            throws IOException
    {
        writer.write("[");
        writer.writeEOL();
        writer.increaseIndentation();
    }

    private void closeArray()
            throws IOException
    {
        writer.writeEOL();
        writer.decreaseIndentation();
        writer.write("]");
    }

    private void openBraces()
            throws IOException
    {
        writer.write("{");
        writer.writeEOL();
        writer.increaseIndentation();
    }

    private void closeBraces()
            throws IOException
    {
        writer.writeEOL();
        writer.decreaseIndentation();
        writer.write("}");
    }

    private void writeComma()
            throws IOException
    {
        writer.write(", ");
        writer.writeEOL();
    }
}
