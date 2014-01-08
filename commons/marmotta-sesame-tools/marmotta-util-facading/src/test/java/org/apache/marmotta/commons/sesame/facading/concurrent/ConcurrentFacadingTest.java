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

package org.apache.marmotta.commons.sesame.facading.concurrent;

import java.util.ConcurrentModificationException;
import java.util.UUID;

import org.apache.marmotta.commons.sesame.facading.AbstractFacadingTest;
import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.commons.sesame.facading.api.Facading;
import org.apache.marmotta.commons.sesame.facading.concurrent.model.FooFacade;
import org.apache.marmotta.commons.sesame.facading.concurrent.model.TypeFacade;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class ConcurrentFacadingTest extends AbstractFacadingTest {

    private URI subject;

    @Before
    public void init() {
        subject = repositoryRDF.getValueFactory().createURI("http://example.com/rdf/" + UUID.randomUUID().toString());
    }

    @Test
    public void testParallelConnections() throws RepositoryException {
        final URI type = repositoryRDF.getValueFactory().createURI(DCTERMS.Agent.stringValue()+"#", UUID.randomUUID().toString());
        final RepositoryConnection cX = repositoryRDF.getConnection(),
                cO = repositoryRDF.getConnection();
        try {
            cX.begin();
            cO.begin();

            cX.add(subject, RDF.TYPE, type);
            Assert.assertTrue (cX.hasStatement(subject, RDF.TYPE, type, false));
            Assert.assertFalse(cO.hasStatement(subject, RDF.TYPE, type, false));

            cX.commit();
            Assert.assertTrue(cO.hasStatement(subject, RDF.TYPE, type, false));

            cO.commit();
        } finally {
            if (cX.isActive()) cX.rollback();
            if (cO.isActive()) cO.rollback();
            cX.close();
            cO.close();
        }        
    }

    @Test
    public void testParallelFacading() throws RepositoryException {
        final RepositoryConnection fc = repositoryRDF.getConnection();
        try {
            fc.begin();
            final Facading facading = FacadingFactory.createFacading(fc);

            final FooFacade ff = facading.createFacade(subject, FooFacade.class);

            Assert.assertNull(ff.getString());
            final String v = UUID.randomUUID().toString(); 
            ff.setString(v);
            Assert.assertEquals(v, ff.getString());

            final RepositoryConnection fc2 = repositoryRDF.getConnection();
            try {
                fc2.begin();

                final Facading f2 = FacadingFactory.createFacading(fc2);
                Assert.assertNull(f2.createFacade(subject, FooFacade.class).getString());

                fc2.commit();
            } finally {
                if (fc2.isActive()) fc2.rollback();
                fc2.close();
            }

            fc.commit();

            final RepositoryConnection fc3 = repositoryRDF.getConnection();
            try {
                fc3.begin();

                final Facading f3 = FacadingFactory.createFacading(fc3);
                Assert.assertEquals(v, f3.createFacade(subject, FooFacade.class).getString());

                fc3.commit();
            } finally {
                if (fc3.isActive()) fc3.rollback();
                fc3.close();
            }
        } finally {
            if (fc.isActive()) fc.rollback();
            fc.close();
        }
    }

    /**
     * Test for MARMOTTA-236
     * @throws RepositoryException
     */
    @Test
    public void testParallelFacadingType() throws RepositoryException {
        final RepositoryConnection mainCon = repositoryRDF.getConnection();
        try {
            mainCon.begin();
            Assert.assertFalse(ResourceUtils.hasType(mainCon, subject, TypeFacade.TYPE));

            final Facading facading = FacadingFactory.createFacading(mainCon);
            Assert.assertFalse(facading.isFacadeable(subject, TypeFacade.class));
            Assert.assertFalse(ResourceUtils.hasType(mainCon, subject, TypeFacade.TYPE));


            final TypeFacade ff = facading.createFacade(subject, TypeFacade.class);
            Assert.assertTrue(facading.isFacadeable(subject, TypeFacade.class));
            Assert.assertTrue(ResourceUtils.hasType(mainCon, subject, TypeFacade.TYPE));

            Assert.assertNull(ff.getTitle());
            final String v = UUID.randomUUID().toString(); 
            ff.setTitle(v);
            Assert.assertEquals(v, ff.getTitle());
            Assert.assertTrue(ResourceUtils.hasType(mainCon, subject, TypeFacade.TYPE));
            Assert.assertTrue(facading.isFacadeable(subject, TypeFacade.class));

            { // before-commit
                final RepositoryConnection subCon_1 = repositoryRDF.getConnection();
                try {
                    subCon_1.begin();
                    Assert.assertFalse(ResourceUtils.hasType(subCon_1, subject, TypeFacade.TYPE));

                    final Facading f_1 = FacadingFactory.createFacading(subCon_1);
                    Assert.assertFalse(f_1.isFacadeable(subject, TypeFacade.class));

                    final TypeFacade tf_1 = f_1.createFacade(subject, TypeFacade.class);
                    Assert.assertNull(tf_1.getTitle());
                    Assert.assertTrue(f_1.isFacadeable(subject, TypeFacade.class));
                    Assert.assertTrue(ResourceUtils.hasType(subCon_1, subject, TypeFacade.TYPE));

                    subCon_1.rollback();
                } finally {
                    if (subCon_1.isActive()) subCon_1.rollback();
                    subCon_1.close();
                }
            }

            mainCon.commit();

            { // after-commit
                final RepositoryConnection subCon_2 = repositoryRDF.getConnection();
                try {
                    subCon_2.begin();
                    Assert.assertTrue(ResourceUtils.hasType(subCon_2, subject, TypeFacade.TYPE));

                    final Facading f_2 = FacadingFactory.createFacading(subCon_2);
                    Assert.assertTrue(f_2.isFacadeable(subject, TypeFacade.class));

                    Assert.assertEquals(v, f_2.createFacade(subject, TypeFacade.class).getTitle());
                    Assert.assertTrue(ResourceUtils.hasType(subCon_2, subject, TypeFacade.TYPE));
                    Assert.assertTrue(f_2.isFacadeable(subject, TypeFacade.class));

                    subCon_2.commit();
                } finally {
                    if (subCon_2.isActive()) subCon_2.rollback();
                    subCon_2.close();
                }
            }
        } catch (ConcurrentModificationException ex) {
            // do nothing, H2 locking
        } finally {
            if (mainCon.isActive()) mainCon.rollback();
            mainCon.close();
        }
    }

}
