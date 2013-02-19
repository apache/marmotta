/*
 * Copyright (c) 2012 Salzburg Research.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

import at.newmedialab.sesame.rio.rss.AtomFormat;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class TestAtomParser {

    private static Logger log = LoggerFactory.getLogger(TestAtomParser.class);

    private String fileName;

    public TestAtomParser(String fileName) {
        this.fileName = fileName;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "iks-blog" });
        return list;
    }



    @Test
    public void runTest() throws Exception {
        log.info("running test {} ...", fileName);

        InputStream atom = this.getClass().getResourceAsStream("testfiles/" + fileName + ".atom");
        InputStream sparql = this.getClass().getResourceAsStream("testfiles/"+fileName+".sparql");
        assumeThat("Could not load testfiles", asList(atom, sparql), everyItem(notNullValue(InputStream.class)));

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        RepositoryConnection connection = repository.getConnection();
        try {
            connection.add(atom, "http://localhost/atom/", AtomFormat.FORMAT);
            connection.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            fail("parsing "+fileName+" failed!");
        }
        assertTrue(connection.size() > 0);

        int count = connection.getStatements(null, null, null, false).asList().size();
        assertTrue(count > 0);

        BooleanQuery sparqlQuery = (BooleanQuery)connection.prepareQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql).replaceAll("http://rdfa.digitalbazaar.com/test-suite/test-cases/xhtml1/rdfa1.1/","http://localhost/rdfa/"));
        assertTrue("SPARQL query evaluation for "+fileName+" failed",sparqlQuery.evaluate());

        connection.close();
        repository.shutDown();
    }

}
