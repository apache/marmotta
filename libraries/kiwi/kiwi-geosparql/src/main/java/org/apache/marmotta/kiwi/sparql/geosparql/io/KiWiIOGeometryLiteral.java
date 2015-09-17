/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.marmotta.kiwi.sparql.geosparql.io;

import org.apache.marmotta.commons.io.DataIO;
import org.apache.marmotta.kiwi.io.KiWiIO;
import org.apache.marmotta.kiwi.io.KiWiIONode;
import org.apache.marmotta.kiwi.model.rdf.KiWiGeometryLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

/**
 * KiWi I/O operations for KiWiGeometryLiteral to efficiently serialize a to a DataOutput destination.
 * Added by MARMOTA 584 (GeoSPARQL Support).
 *
 * @author Sergio Fernández (wikier@apache.org)
 * @author Xavier Sumba (xavier.sumba93@ucuenca.ec)
 */
public final class KiWiIOGeometryLiteral implements KiWiIONode<KiWiGeometryLiteral> {

    public static final int TYPE = 8;

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public Class<KiWiGeometryLiteral> getNodeClass() {
        return KiWiGeometryLiteral.class;
    }

    @Override
    public void write(DataOutput out, KiWiGeometryLiteral literal) throws IOException {
        if(literal == null) {
            out.writeLong(-1L);
        } else {
            out.writeLong(literal.getId());
            KiWiIO.writeContent(out, literal.getContent());
            if(KiWiIO.langTable.containsKey(literal.getLanguage())) {
                out.writeByte(KiWiIO.langTable.get(literal.getLanguage()));
            } else {
                out.writeByte(KiWiIO.LANG_UNKNOWN);
                DataIO.writeString(out, literal.getLanguage());
            }
            KiWiIO.writeURI(out, literal.getType());
            out.writeLong(literal.getCreated().getTime());
        }
    }

    @Override
    public KiWiGeometryLiteral read(DataInput input) throws IOException {
        final long id = input.readLong();
        if(id == -1) {
            return null;
        } else {
            String content = KiWiIO.readContent(input);
            KiWiUriResource dtype = KiWiIO.readURI(input);
            Date created = new Date(input.readLong());
            KiWiGeometryLiteral literal = new KiWiGeometryLiteral(content, dtype, created);
            literal.setId(id);
            return literal;
        }
    }

}
