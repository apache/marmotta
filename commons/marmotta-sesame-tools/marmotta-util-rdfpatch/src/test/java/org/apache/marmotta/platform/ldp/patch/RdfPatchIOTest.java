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
package org.apache.marmotta.platform.ldp.patch;

import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParserImpl;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.vocabulary.DCTERMS;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Testing {@link org.apache.marmotta.platform.ldp.patch.RdfPatchIO}
 */
public class RdfPatchIOTest {

    @Test
    public void testIllustrative() throws Exception {
        RdfPatchParserImpl parser = new RdfPatchParserImpl(getClass().getResourceAsStream("/illustrative.rdfp"));
        checkRoundtrip(parser, Collections.<String, String>emptyMap());
    }

    @Test
    public void testRdfPatch() throws ParseException {
        RdfPatchParserImpl parser = new RdfPatchParserImpl(getClass().getResourceAsStream("/rdf-patch.rdfp"));
        checkRoundtrip(parser, Collections.singletonMap(DCTERMS.PREFIX, DCTERMS.NAMESPACE));
    }

    private void checkRoundtrip(RdfPatchParserImpl parser, Map<String, String> namespaces) throws ParseException {
        final List<PatchLine> patch1 = parser.parsePatch();

        final String serialized = RdfPatchIO.toString(patch1, namespaces);

        parser.ReInit(new StringReader(serialized));
        final List<PatchLine> patch2 = parser.parsePatch();

        for (int i = 0; i < patch1.size(); i++) {
            Assert.assertEquals(patch1.get(i), patch2.get(i));
        }
    }
}
