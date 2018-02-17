/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.loader.core.test;

import java.io.IOException;
import org.apache.marmotta.loader.rio.GeonamesFormat;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class GeonamesParserTest {


    @Test
    public void testParser() throws RDFParseException, IOException, RDFHandlerException {
        MockHandler h = new MockHandler();

        RDFParser p = Rio.createParser(GeonamesFormat.FORMAT);
        p.setRDFHandler(h);

        p.parse(this.getClass().getResourceAsStream("/geonames-sample.txt"), "");

        Assert.assertTrue(h.hasStatements);
    }

    @Test
    public void testFormat() throws ClassNotFoundException {
        Class.forName("org.apache.marmotta.loader.rio.GeonamesFormat");

        RDFFormat f = Rio.getParserFormatForMIMEType("text/vnd.geonames.rdf").orElse(null);

        Assert.assertEquals(GeonamesFormat.FORMAT, f);
    }

    private static class MockHandler extends AbstractRDFHandler {
        boolean hasStatements = false;

        @Override
        public void handleStatement(Statement st) throws RDFHandlerException {
            hasStatements = true;
        }
    }
}
