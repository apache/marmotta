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
import org.apache.marmotta.kiwi.io.KiWiIO;
import org.apache.marmotta.kiwi.model.rdf.KiWiIriResource;

import java.io.IOException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class UriSerializer implements StreamSerializer<KiWiIriResource> {

    @Override
    public int getTypeId() {
        return ExternalizerIds.URI;
    }

    @Override
    public void write(ObjectDataOutput output, KiWiIriResource object) throws IOException {
        KiWiIO.writeURI(output, object);
    }

    @Override
    public KiWiIriResource read(ObjectDataInput input) throws IOException {
        return KiWiIO.readURI(input);
    }

    @Override
    public void destroy() {

    }
}
