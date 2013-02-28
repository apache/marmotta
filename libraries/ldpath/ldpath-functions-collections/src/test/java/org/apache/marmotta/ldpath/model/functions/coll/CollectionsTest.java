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
package org.apache.marmotta.ldpath.model.functions.coll;


import org.apache.marmotta.ldpath.test.AbstractTestBase;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class CollectionsTest extends AbstractTestBase {

    private static final int BAG_SIZE = 4;

    @Parameters(name = "{0}")
    public static List<String[]> data() {
        return Arrays.asList(
                new String[] { "ex:List" },
                new String[] { "ex:Bag" },
                new String[] { "ex:Seq" }
                );
    }

    @Parameter
    public String testContextUri;

    private URI uri;

    @Before
    public void loadData() throws RepositoryException, RDFParseException, IOException {
        super.loadData("data.n3", RDFFormat.N3);

        uri = createURI(testContextUri);
    }

    @Test
    public void testFlatten() throws Exception {
        for (String ldPath : Arrays.asList("foo:hasItems / fn:flatten()", "fn:flatten(foo:hasItems)")) {
            final Collection<Value> nodes = evaluateSelector(ldPath, uri);
            Assert.assertEquals(ldPath, BAG_SIZE, nodes.size());
            Assert.assertThat(ldPath, nodes,
                    CoreMatchers.<Value> hasItems(createURI("ex:1"), createURI("ex:2"), createURI("ex:3"), createURI("ex:4")));
        }
    }

    @Test
    public void testGet() throws Exception {
        for (int i = 0; i < BAG_SIZE + 1; i++) {
            for ( String pattern : Arrays.asList("foo:hasItems / fn:get(\"%d\")", "fn:get(foo:hasItems,\"%d\")")) {
                final String ldPath = String.format(pattern, i);
                final Collection<Value> nodes = evaluateSelector(ldPath, uri);
                if (i < BAG_SIZE) {
                    Assert.assertEquals(ldPath, 1, nodes.size());
                    Assert.assertThat(ldPath, nodes,
                            CoreMatchers.<Value> hasItem(createURI("ex", String.valueOf(i + 1))));
                } else {
                    Assert.assertEquals(ldPath, 0, nodes.size());
                }

            }
        }
    }

    @Test
    public void testSubList() throws Exception {
        for (int i = 0; i <= BAG_SIZE + 1; i++) {
            for (String pattern : Arrays.asList("foo:hasItems / fn:subList(", "fn:subList(foo:hasItems, ")) {
                final String ldPath_1A = String.format("%s\"%d\")", pattern, i);
                final Collection<Value> nodes_1A = evaluateSelector(ldPath_1A, uri);

                final int expStart = Math.max(0, Math.min(i, BAG_SIZE));

                Assert.assertEquals(ldPath_1A, Math.max(BAG_SIZE - expStart, 0), nodes_1A.size());
                for (int e = expStart; e < BAG_SIZE; e++) {
                    Assert.assertThat(ldPath_1A, nodes_1A,
                            CoreMatchers.<Value> hasItem(createURI("ex", String.valueOf(e + 1))));
                }
                for (int j = i; j <= BAG_SIZE + 1; j++) {
                    final String ldPath_2A = String.format("%s\"%d\", \"%d\")", pattern, i, j);
                    final Collection<Value> nodes_2A = evaluateSelector(ldPath_2A, uri);

                    final int expEnd = Math.max(0, Math.min(j, BAG_SIZE));

                    Assert.assertEquals(ldPath_2A, expEnd - expStart, nodes_2A.size());
                    for (int e = expStart; e < expEnd; e++) {
                        Assert.assertThat(ldPath_2A, nodes_2A,
                                CoreMatchers.<Value> hasItem(createURI("ex", String.valueOf(e + 1))));
                    }
                }
            }
        }
    }
}
