package org.apache.marmotta.kiwi.loader;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.kiwi.loader.pgsql.csv.CSVUtil;
import org.apache.marmotta.kiwi.model.rdf.KiWiAnonResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiBooleanLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDateLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDoubleLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiIntLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiStringLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CSVUtilTest {


    protected static Random rnd = new Random();

    protected static long id = 0;


    final static KiWiUriResource TYPE_INT = createURI(XSD.Integer.stringValue());
    final static KiWiUriResource TYPE_DBL = createURI(XSD.Double.stringValue());
    final static KiWiUriResource TYPE_BOOL = createURI(XSD.Boolean.stringValue());
    final static KiWiUriResource TYPE_DATE = createURI(XSD.DateTime.stringValue());





    @Test
    public void testWriteNodes() throws IOException {
        FileOutputStream out = new FileOutputStream("/tmp/nodes.csv");


        List<KiWiNode> nodes = new ArrayList<>(10000);

        nodes.add(TYPE_INT);
        nodes.add(TYPE_DBL);
        nodes.add(TYPE_BOOL);
        nodes.add(TYPE_DATE);

        // randomly create 10000 nodes
        for(int i=0; i<10000; i++) {
            nodes.add(randomObject());
        }

        // flush out nodes
        CSVUtil.flushNodes(nodes,out);

        out.close();

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

}
