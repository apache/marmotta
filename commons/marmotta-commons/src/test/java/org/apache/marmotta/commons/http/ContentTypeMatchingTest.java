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
package org.apache.marmotta.commons.http;

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContentTypeMatchingTest {


    @Test
    public void testMatchNormal1() throws Exception {
        ContentType type1 = new ContentType("application","json");
        ContentType type2 = new ContentType("application","json");

        Assert.assertTrue(type2.matches(type1));
        Assert.assertTrue(type1.matches(type2));
    }


    @Test
    public void testMatchNormal2() throws Exception {
        ContentType type1 = new ContentType("application","json");
        ContentType type2 = new ContentType("application","*");

        Assert.assertTrue(type2.matchesWildcard(type1));
        Assert.assertFalse(type1.matches(type2));
        Assert.assertFalse(type2.matches(type1));
    }

    @Test
    public void testMatchNormal3() throws Exception {
        ContentType type1 = new ContentType("application","json");
        ContentType type2 = new ContentType("*","*");

        Assert.assertTrue(type2.matchesWildcard(type1));
        Assert.assertFalse(type2.matches(type1));
        Assert.assertFalse(type1.matches(type2));
    }

    @Test
    public void testMatchQualifier1() throws Exception {
        ContentType type1 = new ContentType("application","rdf+json");
        ContentType type2 = new ContentType("application","json");

        Assert.assertFalse(type2.matches(type1));
        Assert.assertTrue(type2.matchesSubtype(type1));
        Assert.assertFalse(type1.matchesSubtype(type2));
    }
    @Test
    public void testMatchQualifier2() throws Exception {
        ContentType type1 = new ContentType("application","rdf+json");
        ContentType type2 = new ContentType("application","*");

        Assert.assertTrue(type2.matchesWildcard(type1));
        Assert.assertFalse(type2.matchesSubtype(type1));
        Assert.assertFalse(type1.matchesSubtype(type2));
    }


    @Test
    public void testGetBestMatch1() throws Exception {
        ContentType offered1 = new ContentType("text","html");
        ContentType offered2 = new ContentType("application","rdf+json");
        ContentType offered3 = new ContentType("application","ld+json");

        List<ContentType> offered = new ArrayList<ContentType>(3);
        offered.add(offered1);
        offered.add(offered2);
        offered.add(offered3);

        ContentType accepted1 = new ContentType("application","json");
        ContentType accepted2 = new ContentType("*","*");
        List<ContentType> accepted = new ArrayList<ContentType>(2);
        accepted.add(accepted1);
        accepted.add(accepted2);

        Assert.assertEquals(offered2, MarmottaHttpUtils.bestContentType(offered,accepted));
    }

    @Test
    public void testGetBestMatch2() throws Exception {
        ContentType offered1 = new ContentType("text","html");
        ContentType offered2 = new ContentType("application","rdf+json");
        ContentType offered3 = new ContentType("application","ld+json");

        List<ContentType> offered = new ArrayList<ContentType>(3);
        offered.add(offered1);
        offered.add(offered2);
        offered.add(offered3);

        ContentType accepted1 = new ContentType("application","xml");
        ContentType accepted2 = new ContentType("*","*");
        List<ContentType> accepted = new ArrayList<ContentType>(2);
        accepted.add(accepted1);
        accepted.add(accepted2);

        Assert.assertEquals(offered1, MarmottaHttpUtils.bestContentType(offered,accepted));
    }

    @Test
    public void testGetBestMatch3() throws Exception {
        ContentType offered1 = new ContentType("text","html");
        ContentType offered2 = new ContentType("application","rdf+json");
        ContentType offered3 = new ContentType("application","ld+json");

        List<ContentType> offered = new ArrayList<ContentType>(3);
        offered.add(offered1);
        offered.add(offered2);
        offered.add(offered3);

        ContentType accepted1 = new ContentType("application","ld+json");
        ContentType accepted2 = new ContentType("*","*");
        List<ContentType> accepted = new ArrayList<ContentType>(2);
        accepted.add(accepted1);
        accepted.add(accepted2);

        Assert.assertEquals(offered3, MarmottaHttpUtils.bestContentType(offered,accepted));
    }

}
