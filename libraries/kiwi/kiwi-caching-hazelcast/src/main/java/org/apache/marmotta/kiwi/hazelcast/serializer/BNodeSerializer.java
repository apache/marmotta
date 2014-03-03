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

package org.apache.marmotta.kiwi.hazelcast.serializer;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import org.apache.marmotta.kiwi.model.rdf.KiWiAnonResource;

import java.io.IOException;
import java.util.Date;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class BNodeSerializer implements StreamSerializer<KiWiAnonResource> {



    @Override
    public int getTypeId() {
        return ExternalizerIds.BNODE;
    }

    @Override
    public void write(ObjectDataOutput output, KiWiAnonResource object) throws IOException {
        output.writeLong(object.getId());
        output.writeInt(object.stringValue().length());
        output.writeChars(object.stringValue());
        output.writeLong(object.getCreated().getTime());
    }

    @Override
    public KiWiAnonResource read(ObjectDataInput input) throws IOException {
        long id = input.readLong();
        int len = input.readInt();

        char[] anonId = new char[len];
        for(int i=0; i<len; i++) {
            anonId[i] = input.readChar();
        }

        Date created = new Date(input.readLong());

        KiWiAnonResource r = new KiWiAnonResource(new String(anonId),created);
        r.setId(id);

        return r;
    }

    @Override
    public void destroy() {

    }
}
