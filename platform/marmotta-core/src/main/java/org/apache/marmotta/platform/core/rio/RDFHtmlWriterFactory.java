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
package org.apache.marmotta.platform.core.rio;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

/**
 * OpenRDF RIO RDFWriterFactory Implementation
 * (injected by the ServiceLoader)
 * 
 * @author Sebastian Schaffert
 */
public class RDFHtmlWriterFactory implements RDFWriterFactory {

	public static List<RDFWriter> writers;

	public RDFHtmlWriterFactory() {

	}
	
    /**
     * Returns the RDF format for this factory.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFHtmlFormat.FORMAT;
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied output
     * stream.
     *
     * @param out The OutputStream to write the RDF to.
     */
    @Override
    public RDFWriter getWriter(OutputStream out) {
		return new RDFHtmlWriterImpl(out);
	}

    /**
     * Returns an RDFWriter instance that will write to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     */
    @Override
    public RDFWriter getWriter(Writer writer) {
		return new RDFHtmlWriterImpl(writer);
    }

}
