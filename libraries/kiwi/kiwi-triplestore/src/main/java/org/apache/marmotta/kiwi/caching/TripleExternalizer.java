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

import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.persistence.KiWiPersistence;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;

/**
 * An externalizer for Infinispan allowing to more efficiently transport triples by only serializing the node
 * IDs instead of the whole nodes.
 */
public class TripleExternalizer implements AdvancedExternalizer<KiWiTriple> {

    private static Logger log = LoggerFactory.getLogger(TripleExternalizer.class);

    private KiWiPersistence persistence;

    public TripleExternalizer(KiWiPersistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public Set<Class<? extends KiWiTriple>> getTypeClasses() {
        return Util.<Class<? extends KiWiTriple>>asSet(KiWiTriple.class);
    }

    @Override
    public Integer getId() {
        return 13;
    }

    @Override
    public void writeObject(ObjectOutput output, KiWiTriple object) throws IOException {
        output.writeLong(object.getId());
        output.writeLong(object.getSubject().getId());
        output.writeLong(object.getPredicate().getId());
        output.writeLong(object.getObject().getId());
        output.writeLong(object.getContext() != null ? object.getContext().getId() : -1L);
        output.writeLong(object.getCreator() != null ? object.getCreator().getId() : -1L);
        output.writeBoolean(object.isDeleted());
        output.writeBoolean(object.isInferred());
        output.writeBoolean(object.isNewTriple());
        output.writeLong(object.getCreated().getTime());
        if(object.getDeletedAt() != null) {
            output.writeLong(object.getDeletedAt().getTime());
        } else {
            output.writeLong(0);
        }
    }

    @Override
    public KiWiTriple readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        try {
            KiWiConnection con = persistence.getConnection();
            try {
                KiWiTriple result = new KiWiTriple();
                result.setId(input.readLong());
                result.setSubject((KiWiResource) con.loadNodeById(input.readLong()));
                result.setPredicate((KiWiUriResource) con.loadNodeById(input.readLong()));
                result.setObject(con.loadNodeById(input.readLong()));

                long contextId = input.readLong();
                if(contextId > 0) {
                    result.setContext((KiWiResource) con.loadNodeById(contextId));
                }
                long creatorId = input.readLong();
                if(creatorId > 0) {
                    result.setCreator((KiWiResource) con.loadNodeById(creatorId));
                }

                result.setDeleted(input.readBoolean());
                result.setInferred(input.readBoolean());
                result.setNewTriple(input.readBoolean());

                result.setCreated(new Date(input.readLong()));

                long deletedAt = input.readLong();
                if(deletedAt > 0) {
                    result.setDeletedAt(new Date(deletedAt));
                }


                return result;
            } finally {
                con.commit();
                con.close();
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }
}
