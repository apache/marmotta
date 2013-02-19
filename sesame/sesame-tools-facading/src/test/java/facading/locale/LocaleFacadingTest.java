package facading.locale;


import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import at.newmedialab.sesame.facading.FacadingFactory;
import at.newmedialab.sesame.facading.api.Facading;
import facading.AbstractFacadingTest;
import facading.locale.model.LocaleFacade;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Locale;

public class LocaleFacadingTest extends AbstractFacadingTest {

    @Test
    public void testWithLocale() throws RepositoryException {
        final Locale de = Locale.GERMAN, en = Locale.ENGLISH, fr = Locale.FRENCH, none = new Locale("none");

        final String lbl = "Label",
                lbl_de = lbl + ": " + de.toString(),
                lbl_en = lbl + ": " + en.toString(),
                lbl_fr = lbl + ": " + fr.toString(),
                lbl_none = lbl + ": " + none.toString();

        final RepositoryConnection connection = repositoryRDF.getConnection();
        try {
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

        } finally {
            connection.close();
        }
    }

}
