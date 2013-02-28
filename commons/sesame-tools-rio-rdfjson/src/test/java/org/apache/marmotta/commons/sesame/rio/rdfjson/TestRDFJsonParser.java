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
package org.apache.marmotta.commons.sesame.rio.rdfjson;
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class TestRDFJsonParser {

    private static Logger log = LoggerFactory.getLogger(TestRDFJsonParser.class);

    private String fileName;

    public TestRDFJsonParser(String fileName) {
        this.fileName = fileName;
    }

    // return the list of rdf-NNNN.jsonld files
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        for(int i=1; i<=1; i++) {
            list.add(new Object[] {"test-"+String.format("%03d",i)});
        }
        return list;
    }

    @Test
    public void runTest() throws Exception {
        log.info("running test {} ...", fileName);

        InputStream rdfJSON = this.getClass().getResourceAsStream(fileName+".json");
        InputStream rdfXML = this.getClass().getResourceAsStream(fileName+".rdf");
        assumeThat("Could not load testfiles", asList(rdfJSON, rdfXML), everyItem(notNullValue(InputStream.class)));

        Repository repositoryJSON = new SailRepository(new MemoryStore());
        repositoryJSON.initialize();

        RepositoryConnection connectionJSON = repositoryJSON.getConnection();
        try {
            connectionJSON.add(rdfJSON, "http://localhost/rdfjson/", RDFFormat.RDFJSON);
            connectionJSON.commit();
        } catch(Exception ex) {
            fail("parsing "+fileName+" failed!");
        }
        assertTrue(connectionJSON.size() > 0);


        Repository repositoryRDF = new SailRepository(new MemoryStore());
        repositoryRDF.initialize();

        RepositoryConnection connectionRDF = repositoryRDF.getConnection();
        connectionRDF.add(rdfXML, "http://localhost/rdfjson/", RDFFormat.RDFXML);
        connectionRDF.commit();

        assertEquals(connectionJSON.size(), connectionRDF.size());

        // check each triple in the RDF/XML whether it exists in the RDF/JSON
        for(Statement statement : connectionRDF.getStatements(null,null,null,false).asList()) {
            Resource subject = statement.getSubject() instanceof URI ? statement.getSubject() : null;
            URI predicate = statement.getPredicate();
            Value object = statement.getObject() instanceof BNode ? null : statement.getObject();
            assertTrue("statement "+statement+" not contained in result",connectionJSON.hasStatement(subject,predicate,object,false));
        }

        connectionJSON.close();
        connectionRDF.close();

        repositoryJSON.shutDown();
        repositoryRDF.shutDown();

    }



}