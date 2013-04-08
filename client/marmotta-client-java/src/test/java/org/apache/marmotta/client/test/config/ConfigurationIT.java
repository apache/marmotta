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
package org.apache.marmotta.client.test.config;

import com.google.common.collect.Lists;

import org.apache.marmotta.client.ClientConfiguration;
import org.apache.marmotta.client.clients.ConfigurationClient;
import org.apache.marmotta.client.exception.MarmottaClientException;
import org.apache.marmotta.client.model.config.Configuration;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.core.webservices.config.ConfigurationWebService;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ConfigurationIT {

    private static JettyMarmotta marmotta;

    private static ClientConfiguration config;

    @BeforeClass
    public static void init() {
        marmotta = new JettyMarmotta("/marmotta", ConfigurationWebService.class);

        config = new ClientConfiguration("http://localhost:" + marmotta.getPort() + marmotta.getContext());

    }

    @AfterClass
    public static void tearDown() {
        marmotta.shutdown();
    }

    @Test
    public void testListConfigurationKeys() throws MarmottaClientException, IOException {
        ConfigurationClient client = new ConfigurationClient(config);
        
        Set<String> keys = client.listConfigurationKeys();
        Assert.assertNotNull(keys);
        Assert.assertThat(keys, Matchers.hasItem("kiwi.host"));
    }

    @Test
    public void testListConfigurations() throws MarmottaClientException, IOException {
        ConfigurationClient client = new ConfigurationClient(config);

        Set<Configuration> cfgs1 = client.listConfigurations(null);
        Assert.assertNotNull(cfgs1);
        Assert.assertTrue(cfgs1.size() > 0);


        Set<Configuration> cfgs2 = client.listConfigurations("kiwi");
        Assert.assertNotNull(cfgs2);
        Assert.assertTrue(cfgs2.size() > 0);

        Set<Configuration> cfgs3 = client.listConfigurations("brzlbrnft");
        Assert.assertNotNull(cfgs3);
        Assert.assertTrue(cfgs3.size() == 0);
    }

    @Test
    public void testGetConfiguration() throws MarmottaClientException, IOException {
        ConfigurationClient client = new ConfigurationClient(config);

        Configuration c_version = client.getConfiguration("kiwi.version");
        Assert.assertNotNull(c_version);

        Configuration c_path = client.getConfiguration("kiwi.path");
        Assert.assertNotNull(c_path);
        Assert.assertEquals(marmotta.getContext(), c_path.getString());

        /*
         * is not managed by the webservice anymore
        Configuration c_allow = client.getConfiguration("kiwi.allow_methods");
        Assert.assertNotNull(c_allow);
        Assert.assertThat(c_allow.getList(),Matchers.hasItem("POST"));
        */
    }


    @Test
    public void testSetConfiguration() throws MarmottaClientException, IOException {
        ConfigurationClient client = new ConfigurationClient(config);

        // set a single-value string configuration
        client.setConfiguration("marmottaclient.test.single", "abc");
        Configuration c_single = client.getConfiguration("marmottaclient.test.single");
        Assert.assertNotNull(c_single);
        Assert.assertEquals("abc",c_single.getString());
        client.deleteConfiguration("marmottaclient.test.single");
        c_single = client.getConfiguration("marmottaclient.test.single");
        Assert.assertNull(c_single);

        // set a single-value boolean configuration
        client.setConfiguration("marmottaclient.test.bool",true);
        Configuration c_bool = client.getConfiguration("marmottaclient.test.bool");
        Assert.assertNotNull(c_bool);
        Assert.assertEquals("true",c_bool.getString());
        client.deleteConfiguration("marmottaclient.test.bool");
        c_bool = client.getConfiguration("marmottaclient.test.bool");
        Assert.assertNull(c_bool);


        // set a list value configuration
        client.setConfiguration("marmottaclient.test.list", Lists.newArrayList("abc","efg","hij"));
        Configuration c_list = client.getConfiguration("marmottaclient.test.list");
        Assert.assertNotNull(c_list);
        Assert.assertThat(c_list.getList(), Matchers.hasItem("efg"));
        client.deleteConfiguration("marmottaclient.test.list");
        c_list = client.getConfiguration("marmottaclient.test.list");
        Assert.assertNull(c_list);
    }


}
