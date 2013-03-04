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
package org.apache.marmotta.kiwi.reasoner.test.parser;

import org.apache.marmotta.kiwi.reasoner.model.program.Program;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParser;
import org.apache.marmotta.kiwi.reasoner.parser.KWRLProgramParserBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(Parameterized.class)
public class KWRLProgramParserTest {

    private Repository repository;

    private String filename;

    @Parameterized.Parameters(name="KWRL Program Test {index}: {0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        for(int i=1; i<=4; i++) {
            list.add(new Object[] {"test-"+String.format("%03d",i)});
        }
        return list;
    }


    public KWRLProgramParserTest(String filename) {
        this.filename = filename;
    }

    @Before
    public void setup() throws Exception {
        repository = new SailRepository(new MemoryStore());
        repository.initialize();
    }


    @After
    public void shutdown() throws Exception {
        repository.shutDown();
    }

    @Test
    public void testParseProgram() throws Exception {
        KWRLProgramParserBase parser = new KWRLProgramParser(repository.getValueFactory(), this.getClass().getResourceAsStream(filename+".kwrl"));
        Program p = parser.parseProgram();

        Assert.assertNotNull(p);
        Assert.assertFalse(p.getRules().isEmpty());

    }
}
