package org.apache.marmotta.commons.sesame.rio.jsonld;
/*
 * Copyright (c) 2013 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class TestJSONLdParser {

    private static Logger log = LoggerFactory.getLogger(TestJSONLdParser.class);

    private String fileName;

    public TestJSONLdParser(String fileName) {
        this.fileName = fileName;
    }

    // return the list of rdf-NNNN.jsonld files
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        int[] skip = new int[] {1,2,8,9,10,11,12,14,15,16,17,18,21,22,25};
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        for(int i=1; i<=26; i++) {
            if(Arrays.binarySearch(skip,i) == -1) {
                list.add(new Object[] {"rdf-"+String.format("%04d",i)});
            }
        }
        return list;
    }


    @Test
    public void runTest() throws Exception {
        log.info("running test {} ...", fileName);

        InputStream jsonLD = this.getClass().getResourceAsStream(fileName+".jsonld");
        InputStream sparql = this.getClass().getResourceAsStream(fileName+".sparql");
        assumeThat("Could not load testfiles", asList(jsonLD, sparql), everyItem(notNullValue(InputStream.class)));

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        RepositoryConnection connection = repository.getConnection();
        try {
            connection.add(jsonLD,"http://localhost/jsonld/", RDFFormat.JSONLD);
            connection.commit();
        } catch(Exception ex) {
            fail("parsing "+fileName+" failed!");
        }
        assertTrue(connection.size() > 0);

        int count = connection.getStatements(null, null, null, false).asList().size();
        assertTrue(count > 0);

        BooleanQuery sparqlQuery = (BooleanQuery)connection.prepareQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        assertTrue("SPARQL query evaluation for "+fileName+" failed",sparqlQuery.evaluate());

        connection.close();
        repository.shutDown();
    }
}
