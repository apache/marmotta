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
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class UriExternalizer extends BaseExternalizer<KiWiUriResource> implements AdvancedExternalizer<KiWiUriResource> {

    @Override
    public Set<Class<? extends KiWiUriResource>> getTypeClasses() {
        return Util.<Class<? extends KiWiUriResource>>asSet(KiWiUriResource.class);
    }

    @Override
    public Integer getId() {
        return ExternalizerIds.URI;
    }

    @Override
    public void writeObject(ObjectOutput output, KiWiUriResource object) throws IOException {
        KiWiIO.writeURI(output, object);
    }

    @Override
    public KiWiUriResource readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        return KiWiIO.readURI(input);
    }

}
