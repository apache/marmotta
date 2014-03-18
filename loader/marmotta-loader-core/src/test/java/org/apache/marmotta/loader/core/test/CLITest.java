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
package org.apache.marmotta.loader.core.test;

import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.marmotta.loader.api.LoaderOptions;
import org.apache.marmotta.loader.core.MarmottaLoader;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;

/**
 * Test parsing of command line options
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CLITest {


    @Test(expected = ParseException.class)
    public void testEmpty() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[0]);
    }


    @Test
    public void testInputFile() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-f", "file1.ttl", "-f", "file2.ttl"});

        Assert.assertEquals(2, cfg.getList(LoaderOptions.FILES).size());
        Assert.assertThat(cfg.getList(LoaderOptions.FILES), hasItems((Object)"file1.ttl", "file2.ttl"));
    }


    @Test
    public void testInputDir() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-d", "dir1", "-d", "dir2"});

        Assert.assertEquals(2, cfg.getList(LoaderOptions.DIRS).size());
        Assert.assertThat(cfg.getList(LoaderOptions.DIRS), hasItems((Object)"dir1", "dir2"));
    }


    @Test(expected = ParseException.class)
    public void testInputFileDir() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-f", "file1.ttl", "-d", "dir2"});
    }


    @Test
    public void testCompressionGZIP() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-z", "-f", "file1.ttl" });

        Assert.assertNotNull(cfg.getString(LoaderOptions.COMPRESSION));
        Assert.assertEquals(CompressorStreamFactory.GZIP, cfg.getString(LoaderOptions.COMPRESSION));
    }


    @Test
    public void testCompressionBZIP2() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-j", "-f", "file1.ttl" });

        Assert.assertNotNull(cfg.getString(LoaderOptions.COMPRESSION));
        Assert.assertEquals(CompressorStreamFactory.BZIP2, cfg.getString(LoaderOptions.COMPRESSION));
    }


    @Test(expected = ParseException.class)
    public void testCompressionError() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-j", "-z", "-f", "file1.ttl"});
    }

    @Test
    public void testContext() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-c", "http://localhost/context1",  "-f", "file1.ttl" });

        Assert.assertNotNull(cfg.getString(LoaderOptions.CONTEXT));
        Assert.assertEquals("http://localhost/context1", cfg.getString(LoaderOptions.CONTEXT));
    }

    @Test
    public void testBackendExists() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-B", "dummy",  "-f", "file1.ttl" });

        Assert.assertNotNull(cfg.getString(LoaderOptions.BACKEND));
        Assert.assertEquals("dummy", cfg.getString(LoaderOptions.BACKEND));
    }

    @Test(expected = ParseException.class)
    public void testBackendNotExists() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-B", "blablub",  "-f", "file1.ttl" });
    }

    @Test
    public void testStatisticsWithout() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-s", "-f", "file1.ttl" });

        Assert.assertNotNull(cfg.getProperty(LoaderOptions.STATISTICS_ENABLED));
        Assert.assertNull(cfg.getProperty(LoaderOptions.STATISTICS_GRAPH));

        Assert.assertTrue(cfg.getBoolean(LoaderOptions.STATISTICS_ENABLED));
    }

    @Test
    public void testStatisticsWith() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-s", "file.png", "-f", "file1.ttl" });

        Assert.assertNotNull(cfg.getProperty(LoaderOptions.STATISTICS_ENABLED));
        Assert.assertNotNull(cfg.getProperty(LoaderOptions.STATISTICS_GRAPH));

        Assert.assertTrue(cfg.getBoolean(LoaderOptions.STATISTICS_ENABLED));
        Assert.assertEquals("file.png", cfg.getString(LoaderOptions.STATISTICS_GRAPH));
    }


    @Test
    public void testProperties() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-D", "prop1=value1", "-D", "prop2=value2", "-f", "file1.ttl" });

        Assert.assertNotNull(cfg.getProperty("prop1"));
        Assert.assertNotNull(cfg.getProperty("prop2"));

        Assert.assertEquals("value1", cfg.getString("prop1"));
        Assert.assertEquals("value2", cfg.getString("prop2"));

    }


    @Test
    public void testPluginBoolean() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-E", "-f", "file1.ttl" });

        Assert.assertNotNull(cfg.getProperty("backend.dummy.enabled"));

        Assert.assertTrue(cfg.getBoolean("backend.dummy.enabled"));

    }


    @Test
    public void testPluginSingle() throws ParseException {
        Configuration cfg = MarmottaLoader.parseOptions(new String[] { "-U", "user1", "-f", "file1.ttl" });

        Assert.assertNotNull(cfg.getProperty("backend.dummy.user"));

        Assert.assertEquals("user1", cfg.getString("backend.dummy.user"));

    }

}
