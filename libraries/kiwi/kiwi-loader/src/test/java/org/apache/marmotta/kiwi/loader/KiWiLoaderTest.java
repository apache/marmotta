package org.apache.marmotta.kiwi.loader;

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class KiWiLoaderTest {

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();
    
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
    @Rule
    public final StandardOutputStreamLog stdOut = new StandardOutputStreamLog();
    
    private File confFile;
    private File dataFile;
    private Properties loaderProps;

    @Before
    public void setUp() throws IOException {
        // The config file
        confFile = temp.newFile("loader.properties");
        loaderProps = getLoaderProps();
        FileOutputStream fos = new FileOutputStream(confFile);
        loaderProps.store(fos, "");
        fos.close();
        
        // input file
        dataFile = temp.newFile("test-data.xml");
        copyResourceToFile("/org/apache/marmotta/kiwi/test/demo-data.foaf", dataFile);
        
    }
    
    private Properties getLoaderProps() throws IOException {
        Properties props = new Properties();

        props.setProperty("database.type", "h2");
        props.setProperty("database.url", String.format("jdbc:h2:%s/marmotta;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10", temp.newFolder("db").getAbsolutePath()));
        props.setProperty("database.user", "marmotta");
        props.setProperty("database.password", "marmotta");
        
        props.setProperty("kiwi.context", "http://test.example.com/");
        
        return props;
    }


    @Test
    public void testMain_NoArgs() {
        exit.expectSystemExitWithStatus(3);
        KiWiLoader.main(new String[] {});
        assertThat(stdOut.getLog(), containsString("Cannot import without database connection!"));
    }
    
    @Test
    public void testMain_ConfigNotFound() {
        exit.expectSystemExitWithStatus(1);
        KiWiLoader.main(new String[] {"-c", confFile.getAbsolutePath() + ".not-found"});
        assertThat(stdOut.getLog(), containsString("Could not read system-config.properties:"));
    }
    
    @Test
    public void testMain_FileNotFound() {
        final String fName = dataFile.getAbsolutePath() + ".not-found";
        KiWiLoader.main(new String[] {"-c", confFile.getAbsolutePath(), fName});
        assertThat(stdOut.getLog(), containsString("Could not read file " + fName + ", skipping..."));
    }
    
    @Test
    public void testMain() {
        KiWiLoader.main(new String[] {"-c", confFile.getAbsolutePath(), "-f", "application/rdf+xml", dataFile.getAbsolutePath()});
        assertThat(stdOut.getLog(), containsString("Importing " + dataFile.getAbsolutePath()));
        assertThat(stdOut.getLog(), containsString(String.format("Import completed (%s)", dataFile.getAbsolutePath())));
    }

    @Test
    public void testLoadFile() throws RepositoryException, RDFParseException,
            IOException {
        KiWiTestLoader loader = new KiWiTestLoader(getKiWiConfig(),
                "http://example.com/test/", null);
        loader.initialize();

        loader.load(dataFile.getAbsolutePath(), RDFFormat.RDFXML, false);

        final RepositoryConnection con = loader.getRepository().getConnection();
        try {
            con.begin();
            testRepoContent(con);
            con.commit();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
        loader.shutdown();
    }

    @Test
    public void testLoadFile_GZ() throws RepositoryException, RDFParseException,
            IOException {
        File gz = temp.newFile(dataFile.getName() + ".gz");
        OutputStream os = new GZIPOutputStream(new FileOutputStream(gz));
        FileInputStream is = new FileInputStream(dataFile);
        IOUtils.copy(is, os);
        is.close();
        os.close();
        
        KiWiTestLoader loader = new KiWiTestLoader(getKiWiConfig(),
                "http://example.com/test/", null);
        loader.initialize();

        loader.load(gz.getAbsolutePath(), RDFFormat.RDFXML, true);

        final RepositoryConnection con = loader.getRepository().getConnection();
        try {
            con.begin();
            testRepoContent(con);
            con.commit();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
        loader.shutdown();
    }

    
    private void testRepoContent(RepositoryConnection con) throws RepositoryException {
        final ValueFactory vf = con.getValueFactory();
        assertThat(con.size(), CoreMatchers.equalTo(new Long(34)));
        assertTrue(con.hasStatement(vf.createURI("http://localhost:8080/LMF/resource/hans_meier"), RDF.TYPE, FOAF.PERSON, true));
        assertTrue(con.hasStatement(vf.createURI("http://localhost:8080/LMF/resource/sepp_huber"), RDF.TYPE, FOAF.PERSON, true));
        assertTrue(con.hasStatement(vf.createURI("http://localhost:8080/LMF/resource/anna_schmidt"), RDF.TYPE, FOAF.PERSON, true));
    }

    private KiWiConfiguration getKiWiConfig() {
        final Properties p = loaderProps;
        KiWiConfiguration config = new KiWiConfiguration("loader-test",
                p.getProperty("database.url"), p.getProperty("database.user"),
                p.getProperty("database.password"), new H2Dialect());

        return config;
    }

    @Test
    public void testLoadInputStream() throws RepositoryException, RDFParseException, IOException {
        KiWiTestLoader loader = new KiWiTestLoader(getKiWiConfig(),
                "http://example.com/test/", null);
        loader.initialize();

        loader.load(new FileInputStream(dataFile), RDFFormat.RDFXML);

        final RepositoryConnection con = loader.getRepository().getConnection();
        try {
            con.begin();
            testRepoContent(con);
            con.commit();
        } catch (final Throwable t) {
            con.rollback();
            throw t;
        } finally {
            con.close();
        }
        loader.shutdown();
    }
    
    private static void copyResourceToFile(String resource, File file) throws IOException {
        final FileOutputStream os = new FileOutputStream(file);
        IOUtils.copy(KiWiLoaderTest.class.getResourceAsStream(resource), os);
        os.close();
    }
    
    protected static class KiWiTestLoader extends KiWiLoader {

        public KiWiTestLoader(KiWiConfiguration kiwi, String baseUri,
                String context) {
            super(kiwi, baseUri, context);
        }
        
        public Repository getRepository() {
            return super.repository;
        }
        
    }
    

}
