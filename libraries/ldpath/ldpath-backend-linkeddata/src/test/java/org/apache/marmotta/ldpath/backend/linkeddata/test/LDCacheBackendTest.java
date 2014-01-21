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

package org.apache.marmotta.ldpath.backend.linkeddata.test;

import com.google.common.collect.Collections2;
import org.apache.marmotta.commons.sesame.model.ValueCommons;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.backend.infinispan.LDCachingInfinispanBackend;
import org.apache.marmotta.ldcache.services.test.ng.BaseLDCacheTest;
import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.backend.linkeddata.LDCacheBackend;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LDCacheBackendTest extends BaseLDCacheTest {

    protected static Map<String,String> pathExpressions = new HashMap<>();
    protected static Map<String,String> pathResults     = new HashMap<>();
    static {
        pathExpressions.put(DBPEDIA, "rdfs:label[@en]");
        pathResults.put(DBPEDIA, "Berlin");

        pathExpressions.put(GEONAMES, "<http://www.geonames.org/ontology#name>");
        pathResults.put(GEONAMES, "Embrun");

        pathExpressions.put(MARMOTTA, "<http://usefulinc.com/ns/doap#name>");
        pathResults.put(MARMOTTA, "Apache Marmotta");

        pathExpressions.put(WIKIER, "<http://xmlns.com/foaf/0.1/name>[@es]");
        pathResults.put(WIKIER, "Sergio Fernández");
    }

        /**
         * Needs to be implemented by tests to provide the correct backend. Backend needs to be properly initialised.
         *
         * @return
         */
        @Override
    protected LDCachingBackend createBackend() {
        LDCachingBackend backend = new LDCachingInfinispanBackend();
        backend.initialize();

        return backend;
    }

    protected RDFBackend<Value> createLDPathBackend() {
        LDCacheBackend backend = new LDCacheBackend(ldcache);
        return backend;
    }


    @Override
    protected void testResource(String uri, String sparqlFile) throws Exception {
        super.testResource(uri, sparqlFile);

        if(pathExpressions.containsKey(uri)) {
            LDPath<Value> ldpath = new LDPath<Value>(createLDPathBackend());

            Collection<String> results = Collections2.transform(ldpath.pathQuery(new URIImpl(uri), pathExpressions.get(uri), Collections.EMPTY_MAP), ValueCommons.stringValue());

            Assert.assertThat(results, Matchers.hasItem(pathResults.get(uri)));

        }
    }
}
