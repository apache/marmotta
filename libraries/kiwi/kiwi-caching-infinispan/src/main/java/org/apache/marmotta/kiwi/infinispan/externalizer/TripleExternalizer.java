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

package org.apache.marmotta.kiwi.infinispan.externalizer;

import org.apache.marmotta.kiwi.io.KiWiIO;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

/**
 * An externalizer for Infinispan allowing to more efficiently transport triples by only serializing the node
 * IDs instead of the whole nodes.
 */
public class TripleExternalizer extends BaseExternalizer<KiWiTriple> implements AdvancedExternalizer<KiWiTriple> {


    @Override
    public Set<Class<? extends KiWiTriple>> getTypeClasses() {
        return Util.<Class<? extends KiWiTriple>>asSet(KiWiTriple.class);
    }

    @Override
    public Integer getId() {
        return ExternalizerIds.TRIPLE;
    }

    @Override
    public void writeObject(ObjectOutput output, KiWiTriple object) throws IOException {
        KiWiIO.writeTriple(output,object);
    }

    @Override
    public KiWiTriple readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        return KiWiIO.readTriple(input);
    }
}
