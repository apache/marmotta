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

package org.apache.marmotta.ostrich.model.test;

import org.apache.marmotta.ostrich.model.ProtoURI;
import org.apache.marmotta.ostrich.model.proto.Model;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.URI;

/**
 * Test constructing URIs.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class URITest {

    @Test
    public void testCreateFromString() {
        URI uri = new ProtoURI("http://apache.org/example");

        Assert.assertEquals(uri.stringValue(), "http://apache.org/example");
    }

    @Test
    public void testCreateFromMessage() {
        Model.URI msg = Model.URI.newBuilder().setUri("http://apache.org/example").build();
        URI uri = new ProtoURI(msg);

        Assert.assertEquals(uri.stringValue(), "http://apache.org/example");
    }

    @Test
    public void testEquals() {
        URI uri1 = new ProtoURI("http://apache.org/example");
        URI uri2 = new ProtoURI("http://apache.org/example");

        Assert.assertEquals(uri1, uri2);

    }

}
