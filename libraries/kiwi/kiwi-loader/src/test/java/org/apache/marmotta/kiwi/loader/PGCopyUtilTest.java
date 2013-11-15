package org.apache.marmotta.kiwi.loader;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.loader.pgsql.csv.PGCopyUtil;
import org.apache.marmotta.kiwi.model.rdf.KiWiAnonResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiBooleanLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDateLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDoubleLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiIntLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiStringLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.test.helper.DBConnectionChecker;
import org.apache.marmotta.kiwi.test.junit.KiWiDatabaseRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;
import org.postgresql.copy.PGCopyOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class PGCopyUtilTest {

    private static Logger log = LoggerFactory.getLogger(PGCopyUtilTest.class);

    protected static Random rnd = new Random();

    protected static long id = 0;


    final static KiWiUriResource TYPE_INT = createURI(XSD.Integer.stringValue());
    final static KiWiUriResource TYPE_DBL = createURI(XSD.Double.stringValue());
    final static KiWiUriResource TYPE_BOOL = createURI(XSD.Boolean.stringValue());
    final static KiWiUriResource TYPE_DATE = createURI(XSD.DateTime.stringValue());
    final static KiWiStringLiteral EMPTY = createLiteral("");



    private KiWiStore store;

    private SailRepository repository;

    @Before
    public void setup() throws RepositoryException {
        log.info("creating test setup...");

        KiWiConfiguration psql = KiWiDatabaseRunner.createKiWiConfig("PostgreSQL", new PostgreSQLDialect());
        DBConnectionChecker.checkDatabaseAvailability(psql);

        rnd = new Random();

        store = new KiWiStore(psql);
        repository = new SailRepository(store);
        repository.initialize();
    }

    @After
    public void dropDatabase() throws RepositoryException, SQLException, SailException {
        log.info("cleaning up test setup...");
        if (store != null && store.isInitialized()) {
            assertTrue(store.checkConsistency());
            store.closeValueFactory(); // release all connections before dropping the database
            store.getPersistence().dropDatabase();
            repository.shutDown();
        }
    }




    @Test
    public void testWriteNodes() throws IOException, SQLException {
        KiWiConnection con = store.getPersistence().getConnection();

        PGCopyOutputStream out = new PGCopyOutputStream(PGCopyUtil.getWrappedConnection(con.getJDBCConnection()), "COPY nodes FROM STDIN (FORMAT csv)");

        long start = System.currentTimeMillis();

        List<KiWiNode> nodes = new ArrayList<>(10000);

        nodes.add(TYPE_INT);
        nodes.add(TYPE_DBL);
        nodes.add(TYPE_BOOL);
        nodes.add(TYPE_DATE);
        nodes.add(EMPTY);

        // randomly create 10000 nodes
        for(int i=0; i<10000; i++) {
            nodes.add(randomObject());
        }

        // flush out nodes
        PGCopyUtil.flushNodes(nodes, out);

        out.close();

        long imported = System.currentTimeMillis();

        log.info("imported {} nodes in {} ms", nodes.size(), imported-start);

        // check if database contains the nodes (based on ID)

        PreparedStatement stmt = con.getJDBCConnection().prepareStatement("SELECT * FROM nodes WHERE id = ?");
        for(int i=0; i<nodes.size(); i++) {
            stmt.setLong(1, (long)i);
            ResultSet dbResult = stmt.executeQuery();
            Assert.assertTrue(dbResult.next());
            Assert.assertEquals(nodes.get(i).stringValue(),dbResult.getString("svalue"));
        }

        log.info("checked {} nodes in {} ms", nodes.size(), System.currentTimeMillis()-imported);
    }



    /**
     * Return a random URI, with a 10% chance of returning a URI that has already been used.
     * @return
     */
    protected static KiWiUriResource randomURI() {
        KiWiUriResource r = new KiWiUriResource("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
        r.setId(id++);
        return r;
    }


    protected static KiWiUriResource createURI(String uri) {
        KiWiUriResource r = new KiWiUriResource(uri);
        r.setId(id++);
        return r;
    }

    /**
     * Return a random RDF value, either a reused object (10% chance) or of any other kind.
     * @return
     */
    protected static KiWiNode randomObject() {
        KiWiNode object;
        switch(rnd.nextInt(7)) {
            case 0: object = new KiWiUriResource("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                break;
            case 1: object = new KiWiAnonResource(UUID.randomUUID().toString());
                break;
            case 2: object = new KiWiStringLiteral(RandomStringUtils.randomAscii(40));
                break;
            case 3: object = new KiWiIntLiteral(rnd.nextLong(), TYPE_INT);
                break;
            case 4: object = new KiWiDoubleLiteral(rnd.nextDouble(), TYPE_DBL);
                break;
            case 5: object = new KiWiBooleanLiteral(rnd.nextBoolean(), TYPE_BOOL);
                break;
            case 6: object = new KiWiDateLiteral(new Date(), TYPE_DATE);
                break;
            default: object = new KiWiUriResource("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                break;

        }
        object.setId(id++);
        return object;
    }

    protected static KiWiStringLiteral createLiteral(String data) {
        KiWiStringLiteral r = new KiWiStringLiteral(data);
        r.setId(id++);
        return r;
    }


}
