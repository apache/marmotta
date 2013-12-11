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

package org.apache.marmotta.kiwi.test;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;

import java.util.*;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class ConcurrencyTestBase {

    protected static Repository repository;

    protected static Random rnd;

    private static long runs = 0;

    protected static Logger logger;

    private List<URI> resources = new ArrayList<>();

    private List<Value> objects = new ArrayList<>();

    private Set<Statement> allAddedTriples = new HashSet<>();

    @Rule
    public ConcurrentRule concurrently = new ConcurrentRule();

    @Rule
    public RepeatingRule repeatedly = new RepeatingRule();

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        /**
         * Invoked when a test is about to start
         */
        @Override
        protected void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }

        /**
         * Invoked when a test method finishes (whether passing or failing)
         */
        @Override
        protected void finished(Description description) {
            logger.info("{}: {} added triples, {} removed triples, {} resources reused, {} objects reused", description.getMethodName(), tripleAddCount, tripleRemoveCount, resourcesReused, objectsReused);
        }
    };

    long tripleAddCount = 0;
    long tripleRemoveCount = 0;

    long resourcesReused = 0;
    long objectsReused = 0;

    @Test
    @Concurrent(count = 10)
    @Repeating(repetition = 10)
    public void testConcurrency() throws Exception {
        long run = runs++;
        long removed = 0;

        Set<Statement> addedTriples = new HashSet<>();

        // generate random nodes and triples and add them
        RepositoryConnection con = repository.getConnection();
        try {
            for(int i=0; i< rnd.nextInt(1000); i++) {
                // there is a random chance of deleting a previously added triple, either from this session or from
                // a previous session
                if(allAddedTriples.size() + addedTriples.size() > 0 && rnd.nextInt(10) == 0) {
                    List<Statement> statements = new ArrayList<>();
                    synchronized (allAddedTriples) {
                        statements.addAll(allAddedTriples);
                    }
                    statements.addAll(addedTriples);

                    con.remove(statements.get(rnd.nextInt(statements.size())));

                    removed ++;
                    tripleRemoveCount++;
                } else {
                    URI subject = randomURI();
                    URI predicate = randomURI();
                    Value object = randomObject();
                    Statement stmt = con.getValueFactory().createStatement(subject,predicate,object);
                    con.add(stmt);
                    addedTriples.add(stmt);
                    tripleAddCount++;
                }
            }
            con.commit();

            // commit also all added triples
            synchronized (allAddedTriples) {
                allAddedTriples.addAll(addedTriples);
            }
        } finally {
            con.close();
        }


        logger.info("run {}: triples added: {}; triples removed: {}", run, addedTriples.size(), removed);
    }


    /**
     * Return a random URI, with a 10% chance of returning a URI that has already been used.
     * @return
     */
    protected URI randomURI() {
        synchronized (resources) {
            if(resources.size() > 0 && rnd.nextInt(10) == 0) {
                resourcesReused++;
                // return a resource that was already used
                return resources.get(rnd.nextInt(resources.size()));
            } else {
                URI resource = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                resources.add(resource);
                return resource;
            }
        }
    }

    /**
     * Return a random RDF value, either a reused object (10% chance) or of any other kind.
     * @return
     */
    protected Value randomObject() {
        synchronized (objects) {
            if(objects.size() > 0 && rnd.nextInt(10) == 0) {
                objectsReused++;
                return objects.get(rnd.nextInt(objects.size()));
            } else {
                Value object;
                switch(rnd.nextInt(6)) {
                    case 0: object = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                        break;
                    case 1: object = repository.getValueFactory().createBNode();
                        break;
                    case 2: object = repository.getValueFactory().createLiteral(RandomStringUtils.randomAscii(40));
                        break;
                    case 3: object = repository.getValueFactory().createLiteral(rnd.nextInt());
                        break;
                    case 4: object = repository.getValueFactory().createLiteral(rnd.nextDouble());
                        break;
                    case 5: object = repository.getValueFactory().createLiteral(rnd.nextBoolean());
                        break;
                    default: object = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                        break;

                }
                objects.add(object);
                return object;
            }
        }

    }
}
