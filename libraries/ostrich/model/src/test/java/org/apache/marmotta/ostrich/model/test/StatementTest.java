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

import org.apache.marmotta.ostrich.model.ProtoBNode;
import org.apache.marmotta.ostrich.model.ProtoStatement;
import org.apache.marmotta.ostrich.model.ProtoStringLiteral;
import org.apache.marmotta.ostrich.model.ProtoIRI;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class StatementTest {

    @Test
    public void testCreateFromProtoValues() {
        ProtoBNode s = new ProtoBNode("1234");
        ProtoIRI p = new ProtoIRI("http://apache.org/example/P1");
        ProtoStringLiteral o = new ProtoStringLiteral("Hello, World", "en");
        ProtoIRI c = new ProtoIRI("http://apache.org/example/C1");
        ProtoStatement stmt = new ProtoStatement(s, p, o, c);

        Assert.assertEquals(stmt.getSubject(), s);
        Assert.assertEquals(stmt.getPredicate(), p);
        Assert.assertEquals(stmt.getObject(), o);
        Assert.assertEquals(stmt.getContext(), c);
    }

    @Test
    public void testCreateFromSesameValues() {
        BNode s = SimpleValueFactory.getInstance().createBNode("1234");
        IRI p = SimpleValueFactory.getInstance().createIRI("http://apache.org/example/P1");
        Literal o = SimpleValueFactory.getInstance().createLiteral("Hello, World", "en");
        IRI c = SimpleValueFactory.getInstance().createIRI("http://apache.org/example/C1");
        ProtoStatement stmt = new ProtoStatement(s, p, o, c);

        Assert.assertEquals(stmt.getSubject(), s);
        Assert.assertEquals(stmt.getPredicate(), p);
        Assert.assertEquals(stmt.getObject(), o);
        Assert.assertEquals(stmt.getContext(), c);
    }

}
