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

import info.aduna.iteration.Iterations;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.apache.marmotta.commons.sesame.model.LiteralCommons.getRDFLangStringType;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

/**
 * This test verifies the persistence functionality of the KiWi triple store. 
 *
 * @see org.apache.marmotta.kiwi.persistence.KiWiConnection
 * @see org.apache.marmotta.kiwi.persistence.KiWiPersistence
 * @author Sebastian Schaffert
 */
@RunWith(KiWiDatabaseRunner.class)
public class PersistenceTest {


    private KiWiPersistence persistence;

    private final KiWiConfiguration kiwiConfig;

    public PersistenceTest(KiWiConfiguration kiwiConfig) {
        this.kiwiConfig = kiwiConfig;
    }


    @Before
    public void initDatabase() throws SQLException {
        persistence = new KiWiPersistence(kiwiConfig);
        persistence.initialise();
        persistence.initDatabase();
    }

    @After
    public void dropDatabase() throws SQLException {
        persistence.dropDatabase();
        persistence.shutdown();
    }


    @Test
    public void testCreateDropDatabase() throws SQLException {
        // test if database exists and has a version
        KiWiConnection connection = persistence.getConnection();
        try {
            Assert.assertThat(connection.getDatabaseTables(),hasItems("nodes","triples","namespaces"));
            Assert.assertEquals(KiWiDialect.VERSION, connection.getDatabaseVersion());

            connection.commit();
        } finally {
            connection.close();
        }

    }

    /**
     * Test storing and loading URI nodes.
     *
     * @throws SQLException
     */
    @Test
    public void testStoreUriNode() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiUriResource uri = new KiWiUriResource("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
            connection.storeNode(uri);

            // check if it then has a database ID
            Assert.assertTrue(uri.getId() >= 0);

            KiWiNode testUri1 = connection.loadNodeById(uri.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(uri,testUri1);
            Assert.assertEquals(uri.getId(), testUri1.getId());

            connection.commit();

            KiWiNode testUri2 = connection.loadNodeById(uri.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(uri,testUri2);
            Assert.assertEquals(uri.getId(), testUri2.getId());


            KiWiNode testUri3 = connection.loadUriResource(uri.stringValue());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(uri,testUri3);
            Assert.assertEquals(uri.getId(), testUri3.getId());


            connection.commit();

            Assert.assertEquals(1,Iterations.asList(connection.listResources()).size());


            // clear cache and test again
            persistence.clearCache();
            KiWiNode testUri4 = connection.loadNodeById(uri.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(uri,testUri4);
            Assert.assertEquals(uri.getId(), testUri4.getId());

            KiWiNode testUri5 = connection.loadUriResource(uri.stringValue());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(uri,testUri5);
            Assert.assertEquals(uri.getId(), testUri5.getId());

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertEquals(uri.getId(), result.getLong("id"));
            Assert.assertEquals(uri.stringValue(),result.getString("svalue"));
            Assert.assertEquals("uri",result.getString("ntype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }


    /**
     * Test storing and loading blank nodes.
     *
     * @throws SQLException
     */
    @Test
    public void testStoreBNode() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiAnonResource bnode = new KiWiAnonResource(RandomStringUtils.randomAlphanumeric(8));
            connection.storeNode(bnode);

            // check if it then has a database ID
            Assert.assertTrue(bnode.getId() >= 0);

            KiWiNode testBNode1 = connection.loadNodeById(bnode.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(bnode,testBNode1);
            Assert.assertEquals(bnode.getId(), testBNode1.getId());

            connection.commit();

            KiWiNode testBNode2 = connection.loadNodeById(bnode.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(bnode,testBNode2);
            Assert.assertEquals(bnode.getId(), testBNode2.getId());


            KiWiNode testBNode3 = connection.loadAnonResource(bnode.stringValue());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(bnode,testBNode3);
            Assert.assertEquals(bnode.getId(), testBNode3.getId());


            connection.commit();

            Assert.assertEquals(1,Iterations.asList(connection.listResources()).size());

            // clear cache and test again
            persistence.clearCache();
            KiWiNode testBNode4 = connection.loadNodeById(bnode.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(bnode,testBNode4);
            Assert.assertEquals(bnode.getId(), testBNode4.getId());

            KiWiNode testBNode5 = connection.loadAnonResource(bnode.stringValue());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(bnode,testBNode5);
            Assert.assertEquals(bnode.getId(), testBNode5.getId());

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertEquals((long)bnode.getId(),result.getLong("id"));
            Assert.assertEquals(bnode.stringValue(),result.getString("svalue"));
            Assert.assertEquals("bnode",result.getString("ntype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }


    /**
     * Test storing and loading string literals (without type and language).
     *
     * @throws SQLException
     */
    @Test
    public void testStoreStringLiteralNoType() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            KiWiUriResource   stype   = new KiWiUriResource(Namespaces.NS_XSD+"string");
            connection.storeNode(stype);

            // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiStringLiteral literal = new KiWiStringLiteral(RandomStringUtils.randomAlphanumeric(8),null,stype);
            connection.storeNode(literal);

            // check if it then has a database ID
            Assert.assertTrue(literal.getId() >= 0);

            KiWiNode testLiteral1 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral1);
            Assert.assertEquals(literal.getId(), testLiteral1.getId());

            connection.commit();

            KiWiNode testLiteral2 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral2);
            Assert.assertEquals(literal.getId(), testLiteral2.getId());

            KiWiNode testLiteral3 = connection.loadLiteral(literal.stringValue(), null, stype);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral3);
            Assert.assertEquals(literal.getId(), testLiteral3.getId());

            connection.commit();


            // clear cache and test again
            persistence.clearCache();
            KiWiNode testLiteral4 = connection.loadNodeById(literal.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral4);
            Assert.assertEquals(literal.getId(), testLiteral4.getId());

            KiWiNode testLiteral5 = connection.loadLiteral(literal.stringValue(),null,stype);

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral5);
            Assert.assertEquals(literal.getId(), testLiteral5.getId());

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertTrue(result.next());
            Assert.assertEquals((long) literal.getId(), result.getLong("id"));
            Assert.assertEquals(literal.stringValue(), result.getString("svalue"));
            Assert.assertEquals("string",result.getString("ntype"));
            Assert.assertNull(result.getString("lang"));
            Assert.assertNotNull(result.getObject("ltype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }


    /**
     * Test storing and loading string literals (without type but with language).
     *
     * @throws SQLException
     */
    @Test
    public void testStoreStringLiteralLanguage() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            KiWiUriResource   stype   = new KiWiUriResource(getRDFLangStringType());
            connection.storeNode(stype);

            // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiStringLiteral literal = new KiWiStringLiteral(RandomStringUtils.randomAlphanumeric(8), Locale.ENGLISH, stype);
            connection.storeNode(literal);

            // check if it then has a database ID
            Assert.assertTrue(literal.getId() >= 0);

            KiWiNode testLiteral1 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral1);
            Assert.assertEquals(literal.getId(), testLiteral1.getId());

            connection.commit();

            KiWiNode testLiteral2 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral2);
            Assert.assertEquals(literal.getId(), testLiteral2.getId());

            KiWiNode testLiteral3 = connection.loadLiteral(literal.stringValue(),Locale.ENGLISH.getLanguage(),null);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral3);
            Assert.assertEquals(literal.getId(), testLiteral3.getId());

            connection.commit();


            // clear cache and test again
            persistence.clearCache();
            KiWiNode testLiteral4 = connection.loadNodeById(literal.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral4);
            Assert.assertEquals(literal.getId(), testLiteral4.getId());

            KiWiNode testLiteral5 = connection.loadLiteral(literal.stringValue(),Locale.ENGLISH.getLanguage(),null);

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral5);
            Assert.assertEquals(literal.getId(), testLiteral5.getId());

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertTrue(result.next());
            Assert.assertEquals((long) literal.getId(), result.getLong("id"));
            Assert.assertEquals(literal.stringValue(), result.getString("svalue"));
            Assert.assertEquals("string",result.getString("ntype"));
            Assert.assertEquals(Locale.ENGLISH.getLanguage(),result.getString("lang"));
            Assert.assertNotNull(result.getObject("ltype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }


    /**
     * Test storing and loading string literals (with type and without language).
     *
     * @throws SQLException
     */
    @Test
    public void testStoreStringLiteralType() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            KiWiUriResource uri = new KiWiUriResource("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));

            // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiStringLiteral literal = new KiWiStringLiteral(RandomStringUtils.randomAlphanumeric(8), null, uri);
            connection.storeNode(literal);

            // check if it then has a database ID
            Assert.assertTrue(literal.getId() >= 0);

            KiWiNode testLiteral1 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral1);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral1).getType());
            Assert.assertEquals(literal.getId(), testLiteral1.getId());

            connection.commit();

            KiWiNode testLiteral2 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral2);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral2).getType());
            Assert.assertEquals(literal.getId(), testLiteral2.getId());

            KiWiNode testLiteral3 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral3);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral3).getType());
            Assert.assertEquals(literal.getId(), testLiteral3.getId());

            connection.commit();


            // clear cache and test again
            persistence.clearCache();
            KiWiNode testLiteral4 = connection.loadNodeById(literal.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral4);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral4).getType());
            Assert.assertEquals(literal.getId(), testLiteral4.getId());

            KiWiNode testLiteral5 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral5);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral5).getType());
            Assert.assertEquals(literal.getId(), testLiteral5.getId());

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes WHERE ntype='string'");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertEquals((long)literal.getId(),result.getLong("id"));
            Assert.assertEquals(literal.stringValue(),result.getString("svalue"));
            Assert.assertEquals("string",result.getString("ntype"));
            Assert.assertNull(result.getString("lang"));
            Assert.assertEquals((long) uri.getId(), result.getLong("ltype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }


    /**
     * Test storing and loading string literals (with type and without language).
     *
     * @throws SQLException
     */
    @Test
    public void testStoreBigStringLiteral() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            KiWiUriResource uri = new KiWiUriResource("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));

            // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiStringLiteral literal = new KiWiStringLiteral(RandomStringUtils.randomAlphanumeric(16384), null, uri);
            connection.storeNode(literal);

            // check if it then has a database ID
            Assert.assertTrue(literal.getId() >= 0);

            KiWiNode testLiteral1 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral1);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral1).getType());
            //Assert.assertTrue(literal == testLiteral1);

            connection.commit();

            KiWiNode testLiteral2 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral2);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral2).getType());
            //Assert.assertTrue(literal == testLiteral2);

            KiWiNode testLiteral3 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral3);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral3).getType());
            //Assert.assertTrue(literal == testLiteral3);

            connection.commit();


            // clear cache and test again
            persistence.clearCache();
            KiWiNode testLiteral4 = connection.loadNodeById(literal.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral4);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral4).getType());
            //Assert.assertTrue(literal != testLiteral4);

            KiWiNode testLiteral5 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral5);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral5).getType());
            //Assert.assertTrue(literal != testLiteral5);

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes WHERE ntype='string'");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertEquals((long)literal.getId(),result.getLong("id"));
            Assert.assertEquals(literal.stringValue(),result.getString("svalue"));
            Assert.assertEquals("string",result.getString("ntype"));
            Assert.assertNull(result.getString("lang"));
            Assert.assertEquals((long) uri.getId(), result.getLong("ltype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }



    /**
     * Test storing and loading string literals (with type and without language).
     *
     * @throws SQLException
     */
    @Test
    public void testStoreIntLiteral() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            KiWiUriResource uri = new KiWiUriResource(Namespaces.NS_XSD + "integer");


            Random rnd = new Random();
            long value = rnd.nextLong();

                    // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiIntLiteral literal = new KiWiIntLiteral(value, uri);
            connection.storeNode(literal);

            // check if it then has a database ID
            Assert.assertTrue(literal.getId() >= 0);

            KiWiNode testLiteral1 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral1);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral1).getType());
            //Assert.assertTrue(literal == testLiteral1);

            connection.commit();

            KiWiNode testLiteral2 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral2);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral2).getType());
            //Assert.assertTrue(literal == testLiteral2);

            KiWiNode testLiteral3 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral3);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral3).getType());
            //Assert.assertTrue(literal == testLiteral3);


            // load by integer value
            KiWiNode testLiteral6 = connection.loadLiteral(value);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral6);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral6).getType());
            //Assert.assertTrue(literal == testLiteral6);


            connection.commit();


            // clear cache and test again
            persistence.clearCache();
            KiWiNode testLiteral4 = connection.loadNodeById(literal.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral4);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral4).getType());
            //Assert.assertTrue(literal != testLiteral4);

            KiWiNode testLiteral5 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral5);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral5).getType());
            //Assert.assertTrue(literal != testLiteral5);


            // load by integer value
            KiWiNode testLiteral7 = connection.loadLiteral(value);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral7);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral7).getType());
            //Assert.assertTrue(literal != testLiteral7);

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes WHERE ntype='int'");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertEquals((long) literal.getId(), result.getLong("id"));
            Assert.assertEquals(literal.stringValue(),result.getString("svalue"));
            Assert.assertEquals(value,result.getLong("ivalue"));
            Assert.assertEquals("int",result.getString("ntype"));
            Assert.assertNull(result.getString("lang"));
            Assert.assertEquals((long) uri.getId(), result.getLong("ltype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }

    /**
     * Test storing and loading string literals (with type and without language).
     *
     * @throws SQLException
     */
    @Test
    public void testStoreDoubleLiteral() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            KiWiUriResource uri = new KiWiUriResource(Namespaces.NS_XSD + "double");


            Random rnd = new Random();
            double value = rnd.nextDouble();

            // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiDoubleLiteral literal = new KiWiDoubleLiteral(value, uri);
            connection.storeNode(literal);

            // check if it then has a database ID
            Assert.assertTrue(literal.getId() >= 0);

            KiWiNode testLiteral1 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral1);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral1).getType());
            //Assert.assertTrue(literal == testLiteral1);

            connection.commit();

            KiWiNode testLiteral2 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral2);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral2).getType());
            //Assert.assertTrue(literal == testLiteral2);

            KiWiNode testLiteral3 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral3);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral3).getType());
            //Assert.assertTrue(literal == testLiteral3);


            // load by integer value
            KiWiNode testLiteral6 = connection.loadLiteral(value);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral6);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral6).getType());
            //Assert.assertTrue(literal == testLiteral6);


            connection.commit();


            // clear cache and test again
            persistence.clearCache();
            KiWiNode testLiteral4 = connection.loadNodeById(literal.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral4);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral4).getType());
            //Assert.assertTrue(literal != testLiteral4);

            KiWiNode testLiteral5 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral5);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral5).getType());
            //Assert.assertTrue(literal != testLiteral5);


            // load by integer value
            KiWiNode testLiteral7 = connection.loadLiteral(value);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral7);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral7).getType());
            //Assert.assertTrue(literal != testLiteral7);

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes WHERE ntype='double'");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertEquals((long) literal.getId(), result.getLong("id"));
            Assert.assertEquals(literal.stringValue(),result.getString("svalue"));
            Assert.assertEquals(value,result.getDouble("dvalue"),0.01);
            Assert.assertEquals("double",result.getString("ntype"));
            Assert.assertNull(result.getString("lang"));
            Assert.assertEquals((long) uri.getId(), result.getLong("ltype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }

    /**
     * Test storing and loading string literals (with type and without language).
     *
     * @throws SQLException
     */
    @Test
    public void testStoreBooleanLiteral() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            KiWiUriResource uri = new KiWiUriResource(Namespaces.NS_XSD + "boolean");


            Random rnd = new Random();
            boolean value = rnd.nextBoolean();
            // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiBooleanLiteral literal = new KiWiBooleanLiteral(value, uri);
            connection.storeNode(literal);

            // check if it then has a database ID
            Assert.assertTrue(literal.getId() >= 0);

            KiWiNode testLiteral1 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral1);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral1).getType());
            //Assert.assertTrue(literal == testLiteral1);

            connection.commit();

            KiWiNode testLiteral2 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral2);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral2).getType());
            //Assert.assertTrue(literal == testLiteral2);

            KiWiNode testLiteral3 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral3);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral3).getType());
            //Assert.assertTrue(literal == testLiteral3);


            // load by integer value
            KiWiNode testLiteral6 = connection.loadLiteral(value);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral6);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral6).getType());
            //Assert.assertTrue(literal == testLiteral6);


            connection.commit();


            // clear cache and test again
            persistence.clearCache();
            KiWiNode testLiteral4 = connection.loadNodeById(literal.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral4);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral4).getType());
            //Assert.assertTrue(literal != testLiteral4);

            KiWiNode testLiteral5 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral5);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral5).getType());
            //Assert.assertTrue(literal != testLiteral5);


            // load by integer value
            KiWiNode testLiteral7 = connection.loadLiteral(value);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral7);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral7).getType());
            //Assert.assertTrue(literal != testLiteral7);

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes WHERE ntype='boolean'");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertEquals((long) literal.getId(), result.getLong("id"));
            Assert.assertEquals(literal.stringValue(),result.getString("svalue"));
            Assert.assertEquals(value,result.getBoolean("bvalue"));
            Assert.assertEquals("boolean",result.getString("ntype"));
            Assert.assertNull(result.getString("lang"));
            Assert.assertEquals((long) uri.getId(), result.getLong("ltype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }

    /**
     * Test storing and loading string literals (with type and without language).
     *
     * @throws SQLException
     */
    @Test
    public void testStoreDateLiteral() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            KiWiUriResource uri = new KiWiUriResource(Namespaces.NS_XSD + "dateTime");


            DateTime value = DateTime.now().withMillisOfSecond(0);
            // add a new URI to the triple store and check if it exists afterwards, before and after commit
            KiWiDateLiteral literal = new KiWiDateLiteral(value, uri);
            connection.storeNode(literal);

            // check if it then has a database ID
            Assert.assertTrue(literal.getId() >= 0);

            KiWiNode testLiteral1 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral1);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral1).getType());
            //Assert.assertTrue(literal == testLiteral1);

            connection.commit();

            KiWiNode testLiteral2 = connection.loadNodeById(literal.getId());

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral2);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral2).getType());
            //Assert.assertTrue(literal == testLiteral2);

            KiWiNode testLiteral3 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral3);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral3).getType());
            //Assert.assertTrue(literal == testLiteral3);


            // load by integer value
            KiWiNode testLiteral6 = connection.loadLiteral(value);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral6);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral6).getType());
            //Assert.assertTrue(literal == testLiteral6);


            connection.commit();


            // clear cache and test again
            persistence.clearCache();
            KiWiNode testLiteral4 = connection.loadNodeById(literal.getId());

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral4);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral4).getType());
            //Assert.assertTrue(literal != testLiteral4);

            KiWiNode testLiteral5 = connection.loadLiteral(literal.stringValue(),null,uri);

            // needs to be equal, but now it should not be the same object!
            Assert.assertEquals(literal,testLiteral5);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral5).getType());
            //Assert.assertTrue(literal != testLiteral5);


            // load by integer value
            KiWiNode testLiteral7 = connection.loadLiteral(value);

            // needs to be equal, and should also be the identical object!
            Assert.assertEquals(literal,testLiteral7);
            Assert.assertEquals(uri,((KiWiLiteral)testLiteral7).getType());
            //Assert.assertTrue(literal != testLiteral7);

            connection.commit();

            // finally do a test on the nodes table, it should contain exactly one entry
            PreparedStatement checkNodeStmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM nodes WHERE ntype='date'");
            ResultSet result = checkNodeStmt.executeQuery();

            Assert.assertTrue(result.next());
            Assert.assertEquals((long) literal.getId(), result.getLong("id"));
            Assert.assertEquals(literal.stringValue(),result.getString("svalue"));
            Assert.assertEquals(value.getMillis(), result.getTimestamp("tvalue", Calendar.getInstance(TimeZone.getTimeZone("UTC"))).getTime());
            Assert.assertEquals("date",result.getString("ntype"));
            Assert.assertNull(result.getString("lang"));
            Assert.assertEquals((long) uri.getId(), result.getLong("ltype"));

            result.close();
            connection.commit();
        } finally {
            connection.close();
        }

    }



    /**
     * Test storing, querying and deleting triples
     */
    @Test
    public void testStoreTriples() throws Exception {
            KiWiConnection connection = persistence.getConnection();
            try {
                KiWiUriResource stype    = new KiWiUriResource(Namespaces.NS_XSD+"string");
                KiWiUriResource subject  = new KiWiUriResource("http://localhost/resource/"+RandomStringUtils.randomAlphanumeric(8));
                KiWiUriResource pred_1   = new KiWiUriResource("http://localhost/predicate/P1");
                KiWiUriResource pred_2   = new KiWiUriResource("http://localhost/predicate/P2");
                KiWiUriResource object_1 = new KiWiUriResource("http://localhost/resource/"+RandomStringUtils.randomAlphanumeric(8));
                KiWiStringLiteral object_2 = new KiWiStringLiteral(RandomStringUtils.randomAlphanumeric(32),null,stype);
                KiWiUriResource context  = new KiWiUriResource("http://localhost/context/"+RandomStringUtils.randomAlphanumeric(8));

                connection.storeNode(stype);
                connection.storeNode(subject);
                connection.storeNode(pred_1);
                connection.storeNode(pred_2);
                connection.storeNode(object_1);
                connection.storeNode(object_2);
                connection.storeNode(context);

                KiWiTriple triple1 = new KiWiTriple(subject,pred_1,object_1,context);
                KiWiTriple triple2 = new KiWiTriple(subject,pred_2,object_2,context);

                connection.storeTriple(triple1);
                connection.storeTriple(triple2);

                // check querying within transaction
                List<Statement> result1 = Iterations.asList(connection.listTriples(subject,null,null,null,false, true));
                Assert.assertThat(result1,hasItems((Statement)triple1,(Statement)triple2));

                Assert.assertEquals(2, connection.getSize());
                Assert.assertEquals(2, connection.getSize(context));
                Assert.assertEquals(0, connection.getSize(subject));

                connection.commit();

                List<Statement> result2 = Iterations.asList(connection.listTriples(subject,null,null,null,false, true));
                Assert.assertThat(result2,hasItems((Statement)triple1,(Statement)triple2));

                Assert.assertEquals(2, connection.getSize());
                Assert.assertEquals(2, connection.getSize(context));
                Assert.assertEquals(0, connection.getSize(subject));

                Assert.assertThat(Iterations.asList(connection.listContexts()), hasItem((KiWiResource)context));

                // clear cache and test again
                persistence.clearCache();

                List<Statement> result3 = Iterations.asList(connection.listTriples(subject,null,null,null,false, true));
                Assert.assertThat(result3,hasItems((Statement)triple1,(Statement)triple2));


                Assert.assertEquals(2, connection.getSize());
                Assert.assertEquals(2, connection.getSize(context));
                Assert.assertEquals(0, connection.getSize(subject));

                // test database contents
                PreparedStatement stmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM triples WHERE deleted = false ORDER BY subject, predicate");
                ResultSet dbResult1 = stmt.executeQuery();

                Assert.assertTrue(dbResult1.next());
                Assert.assertEquals((long) triple1.getId(), dbResult1.getLong("id"));
                Assert.assertEquals((long) triple1.getSubject().getId(), dbResult1.getLong("subject"));
                Assert.assertEquals((long) triple1.getPredicate().getId(), dbResult1.getLong("predicate"));
                Assert.assertEquals((long)triple1.getObject().getId(),dbResult1.getLong("object"));

                Assert.assertTrue(dbResult1.next());
                Assert.assertEquals((long)triple2.getId(),dbResult1.getLong("id"));
                Assert.assertEquals((long)triple2.getSubject().getId(),dbResult1.getLong("subject"));
                Assert.assertEquals((long)triple2.getPredicate().getId(),dbResult1.getLong("predicate"));
                Assert.assertEquals((long)triple2.getObject().getId(),dbResult1.getLong("object"));

                dbResult1.close();

                connection.commit();
            } finally {
                connection.close();
            }

    }

    // TODO: test namespaces
    @Test
    public void testStoreNamespaces() throws SQLException {
        KiWiConnection connection = persistence.getConnection();
        try {
            KiWiNamespace ns1 = new KiWiNamespace("ns1", "http://localhost/ns1/");
            KiWiNamespace ns2 = new KiWiNamespace("ns2", "http://localhost/ns2/");

            connection.storeNamespace(ns1);
            connection.storeNamespace(ns2);

            // check before transaction commit
            Assert.assertEquals(ns1, connection.loadNamespaceByPrefix("ns1"));
            Assert.assertEquals(ns1, connection.loadNamespaceByUri("http://localhost/ns1/"));
            Assert.assertEquals(ns2, connection.loadNamespaceByPrefix("ns2"));
            Assert.assertEquals(ns2, connection.loadNamespaceByUri("http://localhost/ns2/"));
            Assert.assertThat(Iterations.asList(connection.listNamespaces()),hasItems(ns1,ns2));

            connection.commit();

            // check after transaction commit
            Assert.assertEquals(ns1, connection.loadNamespaceByPrefix("ns1"));
            Assert.assertEquals(ns1, connection.loadNamespaceByUri("http://localhost/ns1/"));
            Assert.assertEquals(ns2, connection.loadNamespaceByPrefix("ns2"));
            Assert.assertEquals(ns2, connection.loadNamespaceByUri("http://localhost/ns2/"));
            Assert.assertThat(Iterations.asList(connection.listNamespaces()),hasItems(ns1,ns2));

            // clear cache and check again
            persistence.clearCache();

            Assert.assertEquals(ns1, connection.loadNamespaceByPrefix("ns1"));
            Assert.assertEquals(ns1, connection.loadNamespaceByUri("http://localhost/ns1/"));
            Assert.assertEquals(ns2, connection.loadNamespaceByPrefix("ns2"));
            Assert.assertEquals(ns2, connection.loadNamespaceByUri("http://localhost/ns2/"));
            Assert.assertThat(Iterations.asList(connection.listNamespaces()),hasItems(ns1,ns2));


            PreparedStatement stmt = connection.getJDBCConnection().prepareStatement("SELECT * FROM namespaces");
            ResultSet dbResult1 = stmt.executeQuery();

            Assert.assertTrue(dbResult1.next());
            Assert.assertEquals("ns1",dbResult1.getString("prefix"));
            Assert.assertEquals("http://localhost/ns1/",dbResult1.getString("uri"));

            Assert.assertTrue(dbResult1.next());
            Assert.assertEquals("ns2",dbResult1.getString("prefix"));
            Assert.assertEquals("http://localhost/ns2/",dbResult1.getString("uri"));

        } finally {
            connection.close();
        }
    }
}
