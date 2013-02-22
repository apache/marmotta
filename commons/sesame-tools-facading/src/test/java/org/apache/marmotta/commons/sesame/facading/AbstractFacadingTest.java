package org.apache.marmotta.commons.sesame.facading;

import org.junit.After;
import org.junit.Before;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;

public abstract class AbstractFacadingTest {

    protected Repository repositoryRDF;

    @Before
    public void setup() throws RepositoryException, IOException, RDFParseException {
        repositoryRDF = new SailRepository(new MemoryStore());
        repositoryRDF.initialize();
    }

    @After
    public void tearDown() throws RepositoryException {
        repositoryRDF.shutDown();
    }

}
