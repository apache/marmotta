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

package org.apache.marmotta.kiwi.caching;

import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class UriExternalizer implements AdvancedExternalizer<KiWiUriResource> {

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
        output.writeLong(object.getId());
        output.writeInt(object.stringValue().length());
        output.writeChars(object.stringValue());
        output.writeLong(object.getCreated().getTime());
    }

    @Override
    public KiWiUriResource readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        long id = input.readLong();
        int len = input.readInt();

        char[] uri = new char[len];
        for(int i=0; i<len; i++) {
            uri[i] = input.readChar();
        }

        Date created = new Date(input.readLong());

        KiWiUriResource r = new KiWiUriResource(new String(uri),created);
        r.setId(id);

        return r;
    }
}
