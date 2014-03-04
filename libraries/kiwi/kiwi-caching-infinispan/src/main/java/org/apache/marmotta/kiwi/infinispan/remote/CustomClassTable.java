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

package org.apache.marmotta.kiwi.infinispan.remote;

import org.apache.marmotta.kiwi.infinispan.externalizer.*;
import org.jboss.marshalling.ClassTable;
import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.Unmarshaller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom class table to allow for efficient serialization and deserialization of KiWi triple store objects
 * and their externalizers.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CustomClassTable implements ClassTable {

    Writer writer;

    Map<Integer,Class> classLookup;
    Map<Class,Integer> idLookup;


    public CustomClassTable() {
        classLookup = new HashMap<>();
        idLookup    = new HashMap<>();

        register(new BNodeExternalizer());
        register(new BooleanLiteralExternalizer());
        register(new DateLiteralExternalizer());
        register(new DoubleLiteralExternalizer());
        register(new IntLiteralExternalizer());
        register(new StringLiteralExternalizer());
        register(new TripleExternalizer());
        register(new UriExternalizer());

        classLookup.put(11, BaseExternalizer.class);
        idLookup.put(BaseExternalizer.class,11);

        writer = new Writer() {
            @Override
            public void writeClass(Marshaller marshaller, Class<?> clazz) throws IOException {
                marshaller.writeByte((byte) ((int)idLookup.get(clazz)));
            }
        };

    }

    private void register(BaseExternalizer e) {
        // for each externalizer, we register the externalizer itself using its own ID, as well as the type managed
        // by this externalizer using its ID+1 (we anyways use prime numbers for ids, so this is safe)

        classLookup.put(e.getId(), e.getClass());
        idLookup.put(e.getClass(), e.getId());

        Class type = (Class) e.getTypeClasses().iterator().next();
        classLookup.put(e.getId()+1, type);
        idLookup.put(type,e.getId()+1);
    }

    /**
     * Determine whether the given class reference is a valid predefined reference.
     *
     * @param clazz the candidate class
     * @return the class writer, or {@code null} to use the default mechanism
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    public Writer getClassWriter(Class<?> clazz) throws IOException {
        if(idLookup.containsKey(clazz)) {
            return writer;
        } else {
            return null;
        }
    }

    /**
     * Read a class from the stream.  The class will have been written by the
     * {@link #getClassWriter(Class)} method's {@code Writer} instance, as defined above.
     *
     * @param unmarshaller the unmarshaller to read from
     * @return the class
     * @throws java.io.IOException    if an I/O error occurs
     * @throws ClassNotFoundException if a class could not be found
     */
    @Override
    public Class<?> readClass(Unmarshaller unmarshaller) throws IOException, ClassNotFoundException {
        int id = unmarshaller.readByte();

        return classLookup.get(id);
    }


}
