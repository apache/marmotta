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

package org.apache.marmotta.commons.sesame.facading.locale;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.apache.marmotta.commons.sesame.facading.AbstractFacadingTest;
import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.commons.sesame.facading.api.Facading;
import org.apache.marmotta.commons.sesame.facading.locale.model.LocaleFacade;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class LocaleFacadingTest extends AbstractFacadingTest {

    @Test
    public void testWithLocale() throws RepositoryException {
        final Locale de = Locale.GERMAN, en = Locale.ENGLISH, fr = Locale.FRENCH, none = new Locale("xx", "none");

        final String lbl = "Label",
                lbl_de = lbl + ": " + de.toString(),
                lbl_en = lbl + ": " + en.toString(),
                lbl_fr = lbl + ": " + fr.toString(),
                lbl_none = lbl + ": " + none.toString();

        final RepositoryConnection connection = repositoryRDF.getConnection();
        try {
            connection.begin();
            final Facading facading = FacadingFactory.createFacading(connection);

            final URI uri = connection.getValueFactory().createURI("http://www.example.com/rdf/test/locale");
            final LocaleFacade f = facading.createFacade(uri, LocaleFacade.class);

            f.setLabel(lbl);
            assertEquals(lbl, f.getLabel());
            assertNull(f.getLabel(none));

            f.setLabel(lbl_de, de);
            f.setLabel(lbl_en, en);
            assertEquals(lbl_de, f.getLabel(de));
            assertEquals(lbl_en, f.getLabel(en));
            assertNull(f.getLabel(none));

            f.setLabel(null);
            assertNull(f.getLabel());
            assertNull(f.getLabel(de));
            assertNull(f.getLabel(en));
            assertNull(f.getLabel(fr));
            assertNull(f.getLabel(none));

            f.setLabel(lbl_de, de);
            f.setLabel(lbl_en, en);
            f.setLabel(lbl_fr, fr);
            f.setLabel(lbl_none, none);
            assertEquals(lbl_de, f.getLabel(de));
            assertEquals(lbl_en, f.getLabel(en));
            assertEquals(lbl_fr, f.getLabel(fr));
            assertEquals(lbl_none, f.getLabel(none));

            assertThat(f.getLabel(), anyOf(is(lbl_de), is(lbl_en), is(lbl_fr), is(lbl_none)));

            f.deleteLabel(en);
            assertEquals(lbl_de, f.getLabel(de));
            assertNull(f.getLabel(en));
            assertEquals(lbl_fr, f.getLabel(fr));
            assertEquals(lbl_none, f.getLabel(none));

            f.setLabel(null, fr);
            assertEquals(lbl_de, f.getLabel(de));
            assertNull(f.getLabel(en));
            assertNull(f.getLabel(fr));
            assertEquals(lbl_none, f.getLabel(none));

            f.setLabel(lbl);
            assertEquals(lbl, f.getLabel());
            assertNull(f.getLabel(de));
            assertNull(f.getLabel(en));
            assertNull(f.getLabel(fr));
            assertNull(f.getLabel(none));

            connection.commit();
        } finally {
            connection.close();
        }
    }

}
