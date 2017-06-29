/*
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
package org.apache.marmotta.commons.sesame.model;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.IRI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

@RunWith(Parameterized.class)
public class IRICommonsTest {


    private final String prefix;
    private final String local;
    private IRI uri;
    private String uri_string;

    @Parameters(name = "{0}{1}")
    public static List<String[]> data() {
        final ArrayList<String[]> d = new ArrayList<String[]>();

        d.add(new String[] { "http://www.example.com/foo/", "bar" });
        d.add(new String[] { "http://www.example.com/foo#", "bar" });

        return d;
    }

    public IRICommonsTest(String prefix, String local) {
        this.prefix = prefix;
        this.local = local;
    }

    @Before
    public void setup() throws RepositoryException {
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        uri_string = prefix + local;
        uri = repository.getValueFactory().createIRI(prefix, local);

        repository.shutDown();
    }

    @Test
    public void testSplitNamespace() {
        String[] split = IRICommons.splitNamespace(uri_string);

        assertEquals(2, split.length);
        Assert.assertThat(split, equalTo(new String[] { prefix, local }));
    }

    @Test
    public void testCreateCacheKey() {
        assertEquals(uri_string, IRICommons.createCacheKey(uri));
        assertEquals(uri.stringValue(), IRICommons.createCacheKey(uri_string));
        assertEquals(IRICommons.createCacheKey(uri_string), IRICommons.createCacheKey(uri));
    }

}
