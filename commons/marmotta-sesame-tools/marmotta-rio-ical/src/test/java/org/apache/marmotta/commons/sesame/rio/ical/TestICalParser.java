/*
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
package org.apache.marmotta.commons.sesame.rio.ical;

import java.io.InputStream;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
@RunWith(Parameterized.class)
public class TestICalParser {

    private static Logger log = LoggerFactory.getLogger(TestICalParser.class);

    private String fileName;

    public TestICalParser(String fileName) {
        this.fileName = fileName;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "kmt" });
        list.add(new Object[] { "vacation" });
        return list;
    }
    
    @BeforeClass
    public static void beforeClass() {
        // The test files contain old date format: 
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
    }



    @Test
    public void runTest() throws Exception {
        log.info("running test {} ...", fileName);

        InputStream ical = this.getClass().getResourceAsStream(fileName+".ics");
        InputStream sparql = this.getClass().getResourceAsStream(fileName+".sparql");
        assumeThat("Could not load testfiles", asList(ical, sparql), everyItem(notNullValue(InputStream.class)));

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        RepositoryConnection connection = repository.getConnection();
        try {
            connection.add(ical,"http://localhost/ical/", ICalFormat.FORMAT);
            connection.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            fail("parsing " + fileName + " failed!");
        }
        assertTrue(connection.size() > 0);

        int count = Iterations.asList(connection.getStatements(null, null, null, false)).size();
        assertTrue(count > 0);

        BooleanQuery sparqlQuery = (BooleanQuery)connection.prepareQuery(QueryLanguage.SPARQL, IOUtils.toString(sparql));
        assertTrue("SPARQL query evaluation for "+fileName+" failed",sparqlQuery.evaluate());

        connection.close();
        repository.shutDown();
    }

}
