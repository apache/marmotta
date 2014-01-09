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

package org.apache.marmotta.commons.sesame.facading.foaf;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assume.assumeThat;


import org.apache.marmotta.commons.sesame.facading.AbstractFacadingTest;
import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.commons.sesame.facading.api.Facading;
import org.apache.marmotta.commons.sesame.facading.foaf.model.OnlineAccount;
import org.apache.marmotta.commons.sesame.facading.foaf.model.Person;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Test if facading works for the FOAF examples
 * <p/>
 * Author: Sebastian Schaffert
 */
public class FacadingFoafTest extends AbstractFacadingTest {
    private static Logger log = LoggerFactory.getLogger(FacadingFoafTest.class);

    @Before
    public void loadData() throws RepositoryException, IOException, RDFParseException {

        // load demo data
        InputStream rdfXML = this.getClass().getResourceAsStream("demo-data.foaf");
        assumeThat("Could not load test-data: demo-data.foaf", rdfXML, notNullValue(InputStream.class));

        RepositoryConnection connectionRDF = repositoryRDF.getConnection();
        try {
            connectionRDF.add(rdfXML, "http://localhost/foaf/", RDFFormat.RDFXML);
            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }
    }

    /**
     * Test if we can access the data contained in a FOAF file via the facading
     *
     * @throws Exception
     */
    @Test
    public void testAccessData() throws Exception {

        RepositoryConnection connectionRDF = repositoryRDF.getConnection();
        try {

            // list all resources contained in the repository and create facades over them
            Set<Resource> resources = new HashSet<Resource>();
            RepositoryResult<Statement> triples = connectionRDF.getStatements(null,null,null,true);
            while(triples.hasNext()) {
                resources.add(triples.next().getSubject());
            }
            triples.close();

            Facading facading = FacadingFactory.createFacading(connectionRDF);

            // test individual resource
            URI u_hans_meier = connectionRDF.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");
            URI u_anna_schmidt = connectionRDF.getValueFactory().createURI("http://localhost:8080/LMF/resource/anna_schmidt");
            Person hans_meier = facading.createFacade(u_hans_meier,Person.class);

            Assert.assertEquals("Hans Meier",hans_meier.getName());
            Assert.assertEquals(2,hans_meier.getFriends().size());
            Assert.assertThat(hans_meier.getOnlineAccounts(),
                    CoreMatchers.<OnlineAccount> hasItem(hasProperty("accountName", is("Example"))));

            Assert.assertThat(hans_meier.getFriends(), allOf(
                    CoreMatchers.<Person> hasItem(hasProperty("name", is("Anna Schmidt"))),
                    CoreMatchers.<Person> hasItem(hasProperty("name", is("Sepp Huber")))
                    ));

            // test collection
            Collection<Person> persons = facading.createFacade(resources,Person.class);
            Assert.assertEquals(3, persons.size());
            Assert.assertThat(persons,allOf(
                    CoreMatchers.<Person> hasItem(hasProperty("name", is("Hans Meier"))),
                    CoreMatchers.<Person> hasItem(hasProperty("name", is("Anna Schmidt"))),
                    CoreMatchers.<Person> hasItem(hasProperty("name", is("Sepp Huber")))
                    )
                    );

            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }
    }

    /**
     * Test if we can access the data contained in a FOAF file via the facading
     *
     * @throws Exception
     */
    @Test
    public void testModifyData() throws Exception {
        RepositoryConnection connectionRDF = repositoryRDF.getConnection();
        try {
            // list all resources contained in the repository and create facades over them
            Set<Resource> resources = new HashSet<Resource>();
            RepositoryResult<Statement> triples = connectionRDF.getStatements(null,null,null,true);
            while(triples.hasNext()) {
                resources.add(triples.next().getSubject());
            }
            triples.close();

            Facading facading = FacadingFactory.createFacading(connectionRDF);

            // test individual resource
            URI u_hans_meier = connectionRDF.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");
            Person hans_meier = facading.createFacade(u_hans_meier,Person.class);

            Assert.assertNull(hans_meier.getNick());

            // set nick name and check if it is now set
            hans_meier.setNick("hansi");
            Assert.assertNotNull(hans_meier.getNick());
            Assert.assertEquals("hansi",hans_meier.getNick());

            // check in triple store if the triple is there
            URI p_foaf_nick = connectionRDF.getValueFactory().createURI(Namespaces.NS_FOAF + "nick");
            RepositoryResult<Statement> nicknames = connectionRDF.getStatements(u_hans_meier,p_foaf_nick,null,true);
            Assert.assertTrue(nicknames.hasNext());
            Assert.assertEquals("hansi",nicknames.next().getObject().stringValue());
            nicknames.close();

            // test creating a completely new resource
            URI u_fritz_fischer = connectionRDF.getValueFactory().createURI("http://localhost:8080/LMF/resource/fritz_fischer");
            Person fritz_fischer = facading.createFacade(u_fritz_fischer,Person.class);
            fritz_fischer.setName("Fritz Fischer");

            Assert.assertEquals("Fritz Fischer", fritz_fischer.getName());

            // test if it is now there
            URI p_foaf_name = connectionRDF.getValueFactory().createURI(Namespaces.NS_FOAF + "name");
            RepositoryResult<Statement> names = connectionRDF.getStatements(u_fritz_fischer,p_foaf_name,null,true);
            Assert.assertTrue(names.hasNext());
            Assert.assertEquals("Fritz Fischer",names.next().getObject().stringValue());
            names.close();

            // test extending a collection
            Set<Person> hans_friends = hans_meier.getFriends();
            hans_friends.add(fritz_fischer);
            hans_meier.setFriends(hans_friends);

            Assert.assertEquals(3,hans_meier.getFriends().size());
            Assert.assertThat(hans_meier.getFriends(), allOf(
                    CoreMatchers.<Person> hasItem(hasProperty("name", is("Anna Schmidt"))),
                    CoreMatchers.<Person> hasItem(hasProperty("name", is("Fritz Fischer"))),
                    CoreMatchers.<Person> hasItem(hasProperty("name", is("Sepp Huber")))
                    ));

            Assert.assertTrue(hans_meier.hasFriends());

            connectionRDF.commit();
        } finally {
            connectionRDF.close();
        }
    }

    @Test
    public void testReadInverseRDF() throws RepositoryException {

        final RepositoryConnection connection = repositoryRDF.getConnection();
        try {
            final Facading facading = FacadingFactory.createFacading(connection);

            URI u_hans_meier = connection.getValueFactory().createURI("http://localhost:8080/LMF/resource/hans_meier");
            Person hans_meier = facading.createFacade(u_hans_meier, Person.class);
            Assume.assumeThat("Could not load test-person", hans_meier, notNullValue(Person.class));

            for (OnlineAccount account : hans_meier.getOnlineAccounts()) {
                Assert.assertNotNull(account);
                final Resource accountRSC = account.getDelegate();
                Assert.assertNotNull(accountRSC);

                final OnlineAccount oa = facading.createFacade(accountRSC, OnlineAccount.class);
                Assert.assertNotNull(oa);

                Assert.assertEquals(hans_meier, oa.getHolder());
            }

        } finally {
            connection.close();
        }
    }

    @Test
    public void testWriteInverserRDF() throws RepositoryException {
        final RepositoryConnection connection = repositoryRDF.getConnection();
        try {
            final Facading facading = FacadingFactory.createFacading(connection);

            URI p = connection.getValueFactory().createURI("http://localhost/person");
            Person person = facading.createFacade(p, Person.class);

            URI a = connection.getValueFactory().createURI("http://localhost/account");
            OnlineAccount account = facading.createFacade(a, OnlineAccount.class);

            account.setHolder(person);

            connection.commit();

            Assert.assertThat(person.getOnlineAccounts(), CoreMatchers.hasItem(account));

        } finally {
            connection.close();
        }
    }

    @Test
    public void testAdd() throws RepositoryException {
        final RepositoryConnection connection = repositoryRDF.getConnection();
        try {
            final Facading facading = FacadingFactory.createFacading(connection);

            URI a = connection.getValueFactory().createURI("http://localhost/account");
            OnlineAccount account = facading.createFacade(a, OnlineAccount.class);

            account.addChatId("foo");
            Assert.assertThat(account.getChatId(), hasItem("foo"));
            account.addChatId("bar");
            Assert.assertThat(account.getChatId(), hasItems("foo", "bar"));
            Assert.assertThat(account.getChatId(), hasItems("bar", "foo"));
        } finally {
            connection.close();
        }
    }

}
