/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ldpath.test;


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.marmotta.ldpath.api.selectors.NodeSelector;
import org.apache.marmotta.ldpath.backend.sesame.SesameRepositoryBackend;
import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTestBase {
    protected final static Map<String, String> NSS;
    static {
        Map<String, String> nss = new HashMap<String, String>();

        nss.put("ex", "http://www.example.com/");
        nss.put("foo", "http://localhost/vcab#");

        NSS = Collections.unmodifiableMap(nss);
    }

    protected static String ns(String prefix) {
        final String ns = NSS.get(prefix);
        if (ns == null) { throw new NoSuchElementException("Unknown Prefix '" + prefix + "'"); }
        return ns;
    }

    protected static String ns(String prefix, String local) {
        return ns(prefix) + local;
    }

    protected SesameRepositoryBackend backend;
    protected SailRepository repository;

    @Before
    public void before() throws RepositoryException {

        repository = new SailRepository(new MemoryStore());
        repository.initialize();

        backend = new SesameRepositoryBackend(repository);

    }

    @After
    public void after() throws RepositoryException {
        repository.shutDown();
    }

    final Logger logger =
            LoggerFactory.getLogger(this.getClass());

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        /**
         * Invoked when a test is about to start
         */
        @Override
        protected void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };


    protected LdPathParser<Value> createParserFromResource(String input) throws IOException {
        final URL resource = this.getClass().getResource(input);
        assertThat("Could not load test input data '" + input + "'", resource, CoreMatchers.notNullValue());

        LdPathParser<Value> parser = new LdPathParser<Value>(backend, resource.openStream());
        assertThat("Could not parse ldPath", parser, CoreMatchers.notNullValue());

        return parser;
    }

    protected LdPathParser<Value> createParserFromString(String program) {
        final LdPathParser<Value> parser = new LdPathParser<Value>(backend, new StringReader(program));
        assertThat("Could not parse ldPath", parser, CoreMatchers.notNullValue());

        return parser;
    }

    protected final void loadData(String datafile, RDFFormat format) throws RepositoryException, RDFParseException, IOException {
        loadData(datafile, format, "http://www.example.com/ldpath/");
    }

    protected final void loadData(String datafile, RDFFormat format, String baseURI) throws RepositoryException, RDFParseException, IOException {
        // load demo data
        InputStream data = this.getClass().getResourceAsStream(datafile);
        Assert.assertThat("Could not load test-data: " + datafile, data, notNullValue(InputStream.class));

        final SailRepositoryConnection con = repository.getConnection();
        try {

            con.add(data, baseURI, format);

            con.commit();
        } finally {
            con.close();
        }
    }

    protected Collection<Object> evaluateRule(final String ldPath, URI context) throws ParseException {
        final LdPathParser<Value> parser = createParserFromString(ldPath);
        final FieldMapping<Object, Value> rule = parser.parseRule(NSS);
        final Collection<Object> values = rule.getValues(backend, context);
        return values;
    }

    protected URI createURI(String uri) {
        if (uri.matches("\\w+:[a-zA-z0-9_%-]+")) {
            final String[] s = uri.split(":");
            return repository.getValueFactory().createURI(ns(s[0], s[1]));
        } else {
            return repository.getValueFactory().createURI(uri);
        }
    }

    protected URI createURI(String prefix, String local) {
        return repository.getValueFactory().createURI(ns(prefix, local));
    }

    protected Collection<Value> evaluateSelector(final String ldPath, URI context) throws ParseException {
        final LdPathParser<Value> parser = createParserFromString(ldPath);
        final NodeSelector<Value> sel = parser.parseSelector(NSS);
        final Collection<Value> nodes = sel.select(backend, context, null, null);
        return nodes;
    }

}
