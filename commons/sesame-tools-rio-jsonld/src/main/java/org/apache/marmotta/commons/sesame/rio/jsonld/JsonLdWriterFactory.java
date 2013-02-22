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
package org.apache.marmotta.commons.sesame.rio.jsonld;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

import java.io.OutputStream;
import java.io.Writer;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class JsonLdWriterFactory implements RDFWriterFactory {

    /**
     * Returns the RDF format for this factory.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.JSONLD;
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied output
     * stream.
     *
     * @param out The OutputStream to write the RDF to.
     */
    @Override
    public RDFWriter getWriter(OutputStream out) {
        return new JsonLdWriter(out);
    }

    /**
     * Returns an RDFWriter instance that will write to the supplied writer.
     *
     * @param writer The Writer to write the RDF to.
     */
    @Override
    public RDFWriter getWriter(Writer writer) {
        return new JsonLdWriter(writer);
    }
}
