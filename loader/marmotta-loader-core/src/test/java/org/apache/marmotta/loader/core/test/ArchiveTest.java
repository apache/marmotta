/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.loader.core.test;

import org.apache.marmotta.loader.api.LoaderOptions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@RunWith(Parameterized.class)
public class ArchiveTest extends LoaderTestBase {

    private static Logger log = LoggerFactory.getLogger(ArchiveTest.class);

    public ArchiveTest(String filename) {
        super();

        log.info("running test for archive {}", filename);

        cfg.setProperty(LoaderOptions.ARCHIVES, Collections.singletonList(tempDir.toString() + File.separator + filename));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
                { "demo-data.tar.gz"},
                { "demo-data.zip"},
                { "demo-data.7z"}
        };
        return Arrays.asList(data);
    }


}
