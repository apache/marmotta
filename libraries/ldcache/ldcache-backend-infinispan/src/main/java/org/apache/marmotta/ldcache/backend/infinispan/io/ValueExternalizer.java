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

import org.apache.marmotta.ldcache.backend.infinispan.util.DataIO;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ValueExternalizer implements AdvancedExternalizer<Value> {

    private static Logger log = LoggerFactory.getLogger(ValueExternalizer.class);

    private static final int TYPE_URI = 1;
    private static final int TYPE_BNODE = 2;
    private static final int TYPE_LITERAL = 3;


    public ValueExternalizer() {
    }


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
    public Set<Class<? extends Value>> getTypeClasses() {
        Set<Class<? extends Value>> classes = new HashSet<>();
        classes.add(BNode.class);
        classes.add(URI.class);
        classes.add(Literal.class);
        classes.add(BNodeImpl.class);
        classes.add(URIImpl.class);
        classes.add(LiteralImpl.class);
        classes.add(Value.class);

        return classes;
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
        return 263;
    }

    /**
     * Write the object reference to the stream.
     *
     * @param out the object output to write to
     * @param value the object reference to write
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    public void writeObject(ObjectOutput out, Value value) throws IOException {
        int type = getType(value.getClass());

        out.writeInt(type);
        DataIO.writeString(out, value.stringValue());

        if(type == TYPE_LITERAL) {
            Literal l = (Literal)value;

            DataIO.writeString(out,l.getLanguage());

            if(l.getDatatype() != null) {
                DataIO.writeString(out,l.getDatatype().stringValue());
            } else {
                DataIO.writeString(out,null);
            }
        }

    }

    /**
     * Read an instance from the stream.  The instance will have been written by the
     * {@link #writeObject(java.io.ObjectOutput, Object)} method.  Implementations are free
     * to create instances of the object read from the stream in any way that they
     * feel like. This could be via constructor, factory or reflection.
     *
     * @param in the object input to read from
     * @return the object instance
     * @throws java.io.IOException            if an I/O error occurs
     * @throws ClassNotFoundException if a class could not be found
     */
    @Override
    public Value readObject(ObjectInput in) throws IOException, ClassNotFoundException {
        int type = in.readInt();

        String label = DataIO.readString(in);
        switch (type) {
            case TYPE_URI:
                return new URIImpl(label);
            case TYPE_BNODE:
                return new BNodeImpl(label);
            case TYPE_LITERAL:
                String lang  = DataIO.readString(in);
                String dtype  = DataIO.readString(in);

                if(lang != null) {
                    return new LiteralImpl(label,lang);
                } else if(dtype != null) {
                    return new LiteralImpl(label, new URIImpl(dtype));
                } else {
                    return new LiteralImpl(label);
                }
        }
        throw new ClassNotFoundException("could not find class with type "+type);
    }

    private static <C1 extends Value, C2 extends Value> int compareTypes(Class<C1> clazz1, Class<C2> clazz2) {
        int t1 = getType(clazz1), t2 = getType(clazz2);

        return t1 - t2;
    }


    private static <C extends Value> int getType(Class<C> clazz) {
        int t = 0;
        if(URI.class.isAssignableFrom(clazz)) {
            t = TYPE_URI;
        } else if(BNode.class.isAssignableFrom(clazz)) {
            t = TYPE_BNODE;
        } else {
            t = TYPE_LITERAL;
        }
        return t;
    }
}
