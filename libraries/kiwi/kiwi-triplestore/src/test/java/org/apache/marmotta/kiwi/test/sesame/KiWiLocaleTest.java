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
package org.apache.marmotta.kiwi.test.sesame;

import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sail.KiWiValueFactory;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Literal;
import org.openrdf.sail.SailException;

import java.sql.SQLException;

/**
 * Tests for testing locales against the KiWi Triple store 
 * (and its implementation of the ValueFactory)
 * 
 * @author Sergio Fernández <wikier@apache.org>
 */
@RunWith(KiWiDatabaseRunner.class)
public class KiWiLocaleTest  {

    private final KiWiConfiguration kiwiConfig;
    private KiWiStore store;
    private KiWiValueFactory vf;
    
    public KiWiLocaleTest(KiWiConfiguration kiwiConfig) {
        this.kiwiConfig = kiwiConfig;
    }
    
    @Before
    public void initialize() throws SailException {
        store = new KiWiStore(kiwiConfig);
        store.setDropTablesOnShutdown(true);
    	store.initialize();
    	vf = new KiWiValueFactory(store, "http://example.org");
    }
    
    @After
    public void shutdown() throws SailException, SQLException {
    	store.shutDown();
    	store = null;
    	vf = null;
    }
    
    /** 
     * Tests creating BCP47 literals (see MARMOTTA-115 for further details)
     */
    @Test
    public void createBCP47LiteralsTests() {
    	Literal enLiteral = vf.createLiteral("Hungary", "en");
    	Assert.assertEquals("Hungary", enLiteral.getLabel());
    	Assert.assertEquals("en", enLiteral.getLanguage());
    	Literal warLiteral = vf.createLiteral("Hungary", "war");
    	Assert.assertEquals("Hungary", warLiteral.getLabel());
    	Assert.assertEquals("war", warLiteral.getLanguage());
    }

    /** 
     * Tests creating invalid BCP47 literals (see MARMOTTA-115 for further details)
     */
    @Test
    public void createBCP47LiteralsInvalidTests() {
    	Literal invalidLangLiteral = vf.createLiteral("Hungary", "invalid-bcp47-languagetag");
    	Assert.assertEquals("Hungary", invalidLangLiteral.getLabel());
    	Assert.assertNull(invalidLangLiteral.getLanguage());
    }

//    /** 
//     * Tests adding BCP47 literals (see MARMOTTA-115 for further details)
//     */
//    @Test
//    public void addBCP47LiteralsTests() throws SailException {
//    	SailConnection conn = store.getConnection();
//        try {
//        	conn.begin();
//            conn.commit();
//        } finally {
//            conn.close();
//        }
//    	
//    }
//    
//    /** 
//     * Tests importing BCP47 literals (see MARMOTTA-115 for further details)
//     */
//    @Test
//    public void importBCP47LiteralsTests() throws SailException {
//    	SailConnection connection = store.getConnection();
//    	
//    }

}
