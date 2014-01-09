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

package org.apache.marmotta.commons.sesame.facading.builder;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;


import org.apache.marmotta.commons.sesame.facading.AbstractFacadingTest;
import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.commons.sesame.facading.api.Facading;
import org.apache.marmotta.commons.sesame.facading.builder.model.ExampleFacade;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FacadingPredicateBuilderTest extends AbstractFacadingTest {

    private RepositoryConnection connection;
    private Facading facading;

    @Override
    @Before
    public void setup() throws RepositoryException, IOException, RDFParseException {
        super.setup();

        connection = repositoryRDF.getConnection();
        connection.begin();
        facading = FacadingFactory.createFacading(connection);
    }

    @Test
    public void testPredicateBuilder() throws RepositoryException {
        final URI u = connection.getValueFactory().createURI("http://localhost/repository/testResource1");
        ExampleFacade f = facading.createFacade(u, ExampleFacade.class);

        String title = "Example Title";
        Set<String> tags = new HashSet<String>();
        tags.add("Foo");
        tags.add("Bar");

        f.setTitle(title);
        f.setTags(tags);

        checkStatement(u, ExampleFacade.NS + "title", title);
        checkStatement(u, ExampleFacade.NS + "tag", "Foo");
        checkStatement(u, ExampleFacade.NS + "tag", "Bar");

        Assert.assertEquals(f.getTitle(), title);
        Assert.assertThat(f.getTags(), allOf(hasItem("Foo"), hasItem("Bar")));

        f.addTag("FooBar");
        checkStatement(u, ExampleFacade.NS + "tag", "FooBar");
        Assert.assertThat(f.getTags(), allOf(hasItem("FooBar"), hasItem("Foo"), hasItem("Bar")));

    }

    private void checkStatement(URI s, String prop, String val) throws RepositoryException {
        final URI propURI = connection.getValueFactory().createURI(prop);
        final Literal value = connection.getValueFactory().createLiteral(val);

        Assert.assertTrue(String.format("Did not find Statement '<%s> <%s> \"%s\"'", s.stringValue(), prop, val),
                connection.hasStatement(s, propURI, value, true));
    }

    @Override
    @After
    public void tearDown() throws RepositoryException {
        if (connection != null) {
            connection.commit();
            connection.close();
        }

        super.tearDown();
    }

}
