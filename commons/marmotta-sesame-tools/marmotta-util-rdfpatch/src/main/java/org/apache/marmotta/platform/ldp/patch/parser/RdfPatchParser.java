/*
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
package org.apache.marmotta.platform.ldp.patch.parser;

import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.openrdf.model.ValueFactory;

import java.util.List;

/**
 * Parser for the {@code application/rdf-patch} format.
 *
 * @see <a href="http://afs.github.io/rdf-patch/">http://afs.github.io/rdf-patch/</a>
 */
public interface RdfPatchParser {

    /**
     * Mime-Type: {@value}
     */
    public static final String MIME_TYPE = "application/rdf-patch";

    /**
     * Default File extension for rdf-patch: {@value}
     */
    public static final String FILE_EXTENSION = "rdfp";

    void setValueFactory(ValueFactory vf);

    ValueFactory getValueFactory();

    /**
     * Parse the rdf-patch
     * @return a List of {@link org.apache.marmotta.platform.ldp.patch.model.PatchLine}s
     * @throws ParseException if the patch could not be parsed.
     */
    public List<PatchLine> parsePatch() throws ParseException;
}
