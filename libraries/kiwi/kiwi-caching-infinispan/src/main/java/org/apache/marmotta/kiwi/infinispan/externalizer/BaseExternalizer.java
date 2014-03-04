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

import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.jboss.marshalling.Creator;
import org.jboss.marshalling.Externalizer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Base class merging the functionality from JBoss Marshalling and JBoss Infinispan for externalizers
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseExternalizer<T> implements Externalizer, AdvancedExternalizer<T> {


    /**
     * Write the external representation of an object.  The object's class and the externalizer's class will
     * already have been written.
     *
     * @param subject the object to externalize
     * @param output  the output
     * @throws java.io.IOException if an error occurs
     */
    @Override
    public void writeExternal(Object subject, ObjectOutput output) throws IOException {
        writeObject(output, (T)subject);
    }

    /**
     * Create an instance of a type.  The object may then be initialized from {@code input}, or that may be deferred
     * to the {@code readExternal()} method.  Instances may simply delegate the task to the given {@code Creator}.
     * Note that this method is called only on the leaf class, so externalizers for non-final classes that initialize
     * the instance from the stream need to be aware of this.
     *
     * @param subjectType    the type of object to create
     * @param input          the input
     * @param defaultCreator the configured creator
     * @return the new instance
     * @throws java.io.IOException    if an error occurs
     * @throws ClassNotFoundException if a class could not be found during read
     */
    @Override
    public Object createExternal(Class<?> subjectType, ObjectInput input, Creator defaultCreator) throws IOException, ClassNotFoundException {
        return readObject(input);
    }

    /**
     * Read the external representation of an object.  The object will already be instantiated, but may be uninitialized, when
     * this method is called.
     *
     * @param subject the object to read
     * @param input   the input
     * @throws java.io.IOException    if an error occurs
     * @throws ClassNotFoundException if a class could not be found during read
     */
    @Override
    public void readExternal(Object subject, ObjectInput input) throws IOException, ClassNotFoundException {
        // no-op
    }
}
