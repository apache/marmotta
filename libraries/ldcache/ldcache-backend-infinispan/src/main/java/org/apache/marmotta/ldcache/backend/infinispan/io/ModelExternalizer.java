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

package org.apache.marmotta.ldcache.backend.infinispan.io;

import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.TreeModel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ModelExternalizer implements AdvancedExternalizer<TreeModel> {


    /**
     * Returns a collection of Class instances representing the types that this
     * AdvancedExternalizer can marshall.  Clearly, empty sets are not allowed.
     * The externalizer framework currently requires all individual types to be
     * listed since it does not make assumptions based on super classes or
     * interfaces.
     *
     * @return A set containing the Class instances that can be marshalled.
     */
    @Override
    public Set<Class<? extends TreeModel>> getTypeClasses() {
        return Util.<Class<? extends TreeModel>>asSet(TreeModel.class);
    }

    /**
     * Returns an integer that identifies the externalizer type. This is used
     * at read time to figure out which {@link org.infinispan.commons.marshall.AdvancedExternalizer} should read
     * the contents of the incoming buffer.
     * <p/>
     * Using a positive integer allows for very efficient variable length
     * encoding of numbers, and it's much more efficient than shipping
     * {@link org.infinispan.commons.marshall.AdvancedExternalizer} implementation class information around.
     * Negative values are not allowed.
     * <p/>
     * Implementers of this interface can use any positive integer as long as
     * it does not clash with any other identifier in the system.  You can find
     * information on the pre-assigned identifier ranges in
     * <a href="http://community.jboss.org/docs/DOC-16198">here</a>.
     * <p/>
     * It's highly recommended that maintaining of these identifiers is done
     * in a centralized way and you can do so by making annotations reference
     * a set of statically defined identifiers in a separate class or
     * interface.  Such class/interface gives a global view of the identifiers
     * in use and so can make it easier to assign new ids.
     * <p/>
     * Implementors can optionally avoid giving a meaningful implementation to
     * this method (i.e. return null) and instead rely on XML or programmatic
     * configuration to provide the AdvancedExternalizer id.  If no id can be
     * determined via the implementation or XML/programmatic configuration, an
     * error will be reported.  If an id has been defined both via the
     * implementation and XML/programmatic configuration, the value defined via
     * XML/programmatic configuration will be used ignoring the other.
     *
     * @return A positive identifier for the AdvancedExternalizer.
     */
    @Override
    public Integer getId() {
        return 265;
    }

    /**
     * Write the object reference to the stream.
     *
     * @param output the object output to write to
     * @param object the object reference to write
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    public void writeObject(ObjectOutput output, TreeModel object) throws IOException {
        output.writeInt(object.size());
        for(Statement statement : object) {
            output.writeObject(statement.getSubject());
            output.writeObject(statement.getPredicate());
            output.writeObject(statement.getContext());
            if(statement.getContext() != null) {
                output.writeBoolean(true);
                output.writeObject(statement.getContext());
            } else {
                output.writeBoolean(false);
            }

        }
    }

    /**
     * Read an instance from the stream.  The instance will have been written by the
     * {@link #writeObject(java.io.ObjectOutput, Object)} method.  Implementations are free
     * to create instances of the object read from the stream in any way that they
     * feel like. This could be via constructor, factory or reflection.
     *
     * @param input the object input to read from
     * @return the object instance
     * @throws java.io.IOException            if an I/O error occurs
     * @throws ClassNotFoundException if a class could not be found
     */
    @Override
    public TreeModel readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        TreeModel model = new TreeModel();

        int size = input.readInt();
        for(int i=0; i<size; i++) {
            Resource subject = (Resource) input.readObject();
            URI predicate = (URI) input.readObject();
            Value object = (Value) input.readObject();

            boolean hasContext = input.readBoolean();
            if(hasContext) {
                Resource context = (Resource) input.readObject();

                model.add(new ContextStatementImpl(subject,predicate,object,context));
            } else {
                model.add(new StatementImpl(subject,predicate,object));
            }
        }

        return null;
    }
}
