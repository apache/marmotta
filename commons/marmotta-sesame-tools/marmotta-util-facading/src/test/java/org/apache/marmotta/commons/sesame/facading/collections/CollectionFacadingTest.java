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

package org.apache.marmotta.commons.sesame.facading.collections;

import static org.hamcrest.CoreMatchers.hasItems;


import org.apache.marmotta.commons.sesame.facading.AbstractFacadingTest;
import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.commons.sesame.facading.api.Facading;
import org.apache.marmotta.commons.sesame.facading.collections.model.CollectionFacade;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class CollectionFacadingTest extends AbstractFacadingTest {

    @Test
    public void testCollectionFacading() throws RepositoryException {

        final Random rnd = new Random();
        final Date a, b, c, d, e, now;
        now = new Date(60000*(System.currentTimeMillis()/60000L));

        // Start 10Yrs back;
        final int tenYrsInMin = 10 * 365 * 24 * 60;
        a = new Date(now.getTime() - tenYrsInMin * 60000L);
        b = new Date(a.getTime() + rnd.nextInt(tenYrsInMin) * 60000L);
        c = new Date(a.getTime() + rnd.nextInt(tenYrsInMin) * 60000L);
        d = new Date(a.getTime() + rnd.nextInt(tenYrsInMin) * 60000L);
        e = new Date(a.getTime() + rnd.nextInt(tenYrsInMin) * 60000L);

        final RepositoryConnection connection = repositoryRDF.getConnection();
        try {
            connection.begin();
            final Facading facading = FacadingFactory.createFacading(connection);

            URI uri = connection.getValueFactory().createURI("http://www.example.com/rdf/test/collections");
            CollectionFacade facade = facading.createFacade(uri, CollectionFacade.class);

            facade.setDates(Arrays.asList(a, b, c));
            Assert.assertThat(facade.getDates(), hasItems(a, b, c));

            facade.addDate(e);
            Assert.assertThat(facade.getDates(), hasItems(c, e, b, a));

            facade.setDates(Arrays.asList(a, d, now));
            Assert.assertThat(facade.getDates(), hasItems(a, d, now));
            Assert.assertThat(facade.getDates(), CoreMatchers.not(hasItems(c, e, b)));

            facade.deleteDates();
            Assert.assertEquals(facade.getDates().size(), 0);
            
            connection.commit();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testAutorFacading() throws RepositoryException {
        final RepositoryConnection connection = repositoryRDF.getConnection();

        String a1 = UUID.randomUUID().toString(), a2 = UUID.randomUUID().toString(), a3 = UUID.randomUUID().toString();

        try {
            final Facading facading = FacadingFactory.createFacading(connection);
            connection.begin();

            URI uri = connection.getValueFactory().createURI("http://www.example.com/rdf/test/document");
            CollectionFacade facade = facading.createFacade(uri, CollectionFacade.class);

            facade.setAutors(Arrays.asList(a1, a2));

            facade.addAutor(a3);

            connection.commit();
        } finally {
            connection.close();
        }
    }

}
