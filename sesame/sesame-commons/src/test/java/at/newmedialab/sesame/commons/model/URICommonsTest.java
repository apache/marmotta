package at.newmedialab.sesame.commons.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.util.ArrayList;
import java.util.List;

@RunWith(Parameterized.class)
public class URICommonsTest {


    private final String prefix;
    private final String local;
    private URI uri;
    private String uri_string;

    @Parameters(name = "{0}{1}")
    public static List<String[]> data() {
        final ArrayList<String[]> d = new ArrayList<String[]>();

        d.add(new String[] { "http://www.example.com/foo/", "bar" });
        d.add(new String[] { "http://www.example.com/foo#", "bar" });

        return d;
    }

    public URICommonsTest(String prefix, String local) {
        this.prefix = prefix;
        this.local = local;
    }

    @Before
    public void setup() throws RepositoryException {
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        uri_string = prefix + local;
        uri = repository.getValueFactory().createURI(prefix, local);

        repository.shutDown();
    }

    @Test
    public void testSplitNamespace() {
        String[] split = URICommons.splitNamespace(uri_string);

        assertEquals(2, split.length);
        Assert.assertThat(split, equalTo(new String[] { prefix, local }));
    }

    @Test
    public void testCreateCacheKey() {
        assertEquals(uri_string, URICommons.createCacheKey(uri));
        assertEquals(uri.stringValue(), URICommons.createCacheKey(uri_string));
        assertEquals(URICommons.createCacheKey(uri_string), URICommons.createCacheKey(uri));
    }

}
