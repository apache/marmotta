/**
 *  Copyright (c) 2012 Salzburg Research.
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
package org.apache.marmotta.platform.core.services.prefix;

import org.apache.marmotta.platform.core.api.prefix.PrefixService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Prefix Manager tests
 * 
 * @author Sergio Fernández
 * 
 */
// @RunWith(WeldJUnit4Runner.class)
public class PrefixServiceTests {

    private final String PREFIX = "foo";
    private final String NS = "http://example.org/foo#";
    private final String PREFIX2 = "bar";
    private final String NS2 = "http://example.org/bar#";

    // @Inject
    protected PrefixService prefixService;

    @Before
    public void setup() {
        prefixService = new PrefixServiceImpl();
    }

    @Test
    public void testAddition() {
        int size = prefixService.getMappings().size();
        prefixService.add(PREFIX, NS);
        assertEquals(size + 1, prefixService.getMappings().size());
        assertTrue(prefixService.containsPrefix(PREFIX));
        assertTrue(prefixService.containsNamespace(NS));
        assertEquals(NS, prefixService.getNamespace(PREFIX));
        assertEquals(PREFIX, prefixService.getPrefix(NS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleAddition() {
        int size = prefixService.getMappings().size();
        prefixService.add(PREFIX, NS);
        assertEquals(size + 1, prefixService.getMappings().size());
        prefixService.add(PREFIX, NS);
    }

    @Test
    public void testDoubleForceAddition() {
        int size = prefixService.getMappings().size();
        prefixService.add(PREFIX, NS);
        assertEquals(size + 1, prefixService.getMappings().size());
        assertTrue(prefixService.containsPrefix(PREFIX));
        assertTrue(prefixService.containsNamespace(NS));
        assertEquals(NS, prefixService.getNamespace(PREFIX));
        assertEquals(PREFIX, prefixService.getPrefix(NS));
        prefixService.forceAdd(PREFIX, NS2);
        assertEquals(size + 1, prefixService.getMappings().size());
        assertTrue(prefixService.containsPrefix(PREFIX));
        assertFalse(prefixService.containsNamespace(NS));
        assertTrue(prefixService.containsNamespace(NS2));
        assertEquals(NS2, prefixService.getNamespace(PREFIX));
        assertEquals(PREFIX, prefixService.getPrefix(NS2));
        prefixService.forceAdd(PREFIX2, NS2);
        assertEquals(size + 1, prefixService.getMappings().size());
        assertFalse(prefixService.containsPrefix(PREFIX));
        assertTrue(prefixService.containsPrefix(PREFIX2));
        assertFalse(prefixService.containsNamespace(NS));
        assertTrue(prefixService.containsNamespace(NS2));
        assertEquals(NS2, prefixService.getNamespace(PREFIX2));
        assertEquals(PREFIX2, prefixService.getPrefix(NS2));
    }

    @Test
    public void testMissingMapping() {
        assertFalse(prefixService.containsPrefix(PREFIX));
        assertFalse(prefixService.containsNamespace(NS));
        assertNull(prefixService.getPrefix(NS));
        assertNull(prefixService.getNamespace(PREFIX));
    }

    @Test
    public void validateCurie() {
        int size = prefixService.getMappings().size();
        prefixService.add(PREFIX, NS);
        assertEquals(size + 1, prefixService.getMappings().size());
        String curie = prefixService.getCurie(NS + PREFIX);
        assertNotNull(curie);
        assertEquals(PREFIX + ":" + PREFIX, curie);
    }

    @Test
    public void validateUnregisteredCurie() {
        assertFalse(prefixService.containsNamespace(NS));
        assertNull(prefixService.getCurie(NS + PREFIX));
    }

    @Test
    public void validateWrongCurie() {
        int size = prefixService.getMappings().size();
        prefixService.add(PREFIX, NS);
        assertEquals(size + 1, prefixService.getMappings().size());
        assertNull(prefixService.getCurie(NS));
    }

}
