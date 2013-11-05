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
package org.apache.marmotta.kiwi.reasoner.test.persistence;

import info.aduna.iteration.CloseableIteration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.reasoner.model.program.LiteralField;
import org.apache.marmotta.kiwi.reasoner.model.program.Pattern;
import org.apache.marmotta.kiwi.reasoner.model.program.ResourceField;
import org.apache.marmotta.kiwi.reasoner.model.program.VariableField;
import org.apache.marmotta.kiwi.reasoner.model.query.QueryResult;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningConnection;
import org.apache.marmotta.kiwi.reasoner.persistence.KiWiReasoningPersistence;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * This test verifies the persistence functionality of the reasoning component regarding storing, loading and deleting
 * reasoning programs.
 *
 * @see org.apache.marmotta.kiwi.persistence.KiWiConnection
 * @see org.apache.marmotta.kiwi.persistence.KiWiPersistence
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(KiWiDatabaseRunner.class)
public class PatternQueryTest {

    // string constants for RDF values
    private static final String SUBJECT1 = "http://localhost/resource/S1";
    private static final String SUBJECT2 = "http://localhost/resource/S2";
    private static final String SUBJECT3 = "http://localhost/resource/S3";
    private static final String PREDICATE1 = "http://localhost/resource/P1";
    private static final String PREDICATE2 = "http://localhost/resource/P2";
    private static final String OBJECT1  = "http://localhost/resource/O1";
    private static final String OBJECT2  = "http://localhost/resource/O2";
    private static final String OBJECT3  = "Literal Value 1";
    private static final String OBJECT4  = "Literal Value 2";
    private static final String OBJECT5  = "Literal Value 3";

    private KiWiPersistence persistence;
    private KiWiReasoningPersistence rpersistence;

    private Repository repository;
    private final KiWiConfiguration config;


    public PatternQueryTest(KiWiConfiguration config) {
        this.config = config;
    }


    @Before
    public void initDatabase() throws Exception {
        KiWiStore store = new KiWiStore(config);

        repository = new SailRepository(store);
        repository.initialize();

        persistence = store.getPersistence();


        rpersistence = new KiWiReasoningPersistence(persistence, repository.getValueFactory());
        rpersistence.initDatabase();

        // add some triples
        RepositoryConnection con = repository.getConnection();
        try {
            ValueFactory v = con.getValueFactory();

            con.add(v.createURI(SUBJECT1), v.createURI(PREDICATE1), v.createURI(SUBJECT2));
            con.add(v.createURI(SUBJECT1), v.createURI(PREDICATE2), v.createLiteral(OBJECT3));
            con.add(v.createURI(SUBJECT2), v.createURI(PREDICATE1), v.createURI(OBJECT2));
            con.add(v.createURI(SUBJECT2), v.createURI(PREDICATE2), v.createLiteral(OBJECT4));
            con.add(v.createURI(SUBJECT3), v.createURI(PREDICATE2), v.createLiteral(OBJECT5));

            con.commit();
        } finally {
            con.close();
        }

    }

    @After
    public void dropDatabase() throws Exception {
        rpersistence.dropDatabase();
        persistence.dropDatabase();
        repository.shutDown();
    }

    final Logger logger =
            LoggerFactory.getLogger(PatternQueryTest.class);


    // test the method for querying patterns by:
    // - evaluating a single pattern without variables (no bindings but justifications non-empty)
    // - evaluating a single pattern with variables    (bindings and justifications non-empty)
    // - evaluating a collection of patterns           (more complex SQL query)

    /**
     * Test a single pattern with constant fields
     */
    @Test
    public void testSingleConstantPattern() throws Exception {
        ValueFactory v = repository.getValueFactory();
        URI subject = v.createURI(SUBJECT1);
        URI predicate = v.createURI(PREDICATE2);
        Literal object = v.createLiteral(OBJECT3);

        Pattern p = new Pattern(new ResourceField(subject), new ResourceField(predicate), new LiteralField(object));
        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            List<QueryResult> results = asList(connection.query(Collections.singleton(p), null, null, null, true));
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(1,results.get(0).getJustifications().size());

            KiWiTriple justification = results.get(0).getJustifications().iterator().next();
            Assert.assertEquals(subject, justification.getSubject());
            Assert.assertEquals(predicate, justification.getPredicate());
            Assert.assertEquals(object, justification.getObject());

            connection.commit();
        } finally {
            connection.close();
        }
    }

    /**
     * Test a single pattern with constant fields and variables
     */
    @Test
    public void testSingleVariablePattern() throws Exception {
        ValueFactory v = repository.getValueFactory();
        URI predicate = v.createURI(PREDICATE2);

        Pattern p = new Pattern(new VariableField("X"), new ResourceField(predicate), new VariableField("Y"));
        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            List<QueryResult> results = asList(connection.query(Collections.singleton(p), null, null, null, true));
            Assert.assertEquals(3, results.size());

            for(int i=0; i<3; i++) {
                Assert.assertEquals(1,results.get(i).getJustifications().size());

                KiWiTriple justification = results.get(i).getJustifications().iterator().next();
                Assert.assertEquals(predicate, justification.getPredicate());
            }

            connection.commit();
        } finally {
            connection.close();
        }
    }

    /**
     * Test a conjunction of two patterns with variables and constants
     */
    @Test
    public void testMultipleVariablePattern() throws Exception {
        ValueFactory v = repository.getValueFactory();
        URI predicate1 = v.createURI(PREDICATE1);
        URI predicate2 = v.createURI(PREDICATE2);

        VariableField x =  new VariableField("X");
        VariableField y =  new VariableField("Y");
        VariableField z =  new VariableField("Z");

        Pattern p1 = new Pattern(x, new ResourceField(predicate1), y);
        Pattern p2 = new Pattern(y, new ResourceField(predicate1), z);
        KiWiReasoningConnection connection = rpersistence.getConnection();
        try {
            List<QueryResult> results = asList(connection.query(ImmutableSet.of(p1,p2), null, null, null, true));
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(2, results.get(0).getJustifications().size());


            Assert.assertEquals(SUBJECT1, results.get(0).getBindings().get(x).stringValue());
            Assert.assertEquals(SUBJECT2, results.get(0).getBindings().get(y).stringValue());
            Assert.assertEquals(OBJECT2, results.get(0).getBindings().get(z).stringValue());

            KiWiTriple justification1 = results.get(0).getJustifications().iterator().next();
            Assert.assertEquals(predicate1, justification1.getPredicate());

            connection.commit();
        } finally {
            connection.close();
        }


    }



    /**
     * Workaround for https://openrdf.atlassian.net/browse/SES-1702 in Sesame 2.7.0-beta1
     * @param <E>
     * @return
     */
    public static <E,X extends Exception> List<E> asList(CloseableIteration<E,X> result) throws Exception {
        ArrayList<E> collection = new ArrayList<E>();
        try {
            while (result.hasNext()) {
                collection.add(result.next());
            }

            return collection;
        } finally {
            result.close();
        }
    }

}
