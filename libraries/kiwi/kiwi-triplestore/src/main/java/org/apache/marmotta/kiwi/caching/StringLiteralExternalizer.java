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

import org.apache.marmotta.kiwi.model.rdf.KiWiStringLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class StringLiteralExternalizer implements AdvancedExternalizer<KiWiStringLiteral> {

    @Override
    public Set<Class<? extends KiWiStringLiteral>> getTypeClasses() {
        return Util.<Class<? extends KiWiStringLiteral>>asSet(KiWiStringLiteral.class);
    }

    @Override
    public Integer getId() {
        return ExternalizerIds.STRING_LITERAL;
    }

    @Override
    public void writeObject(ObjectOutput output, KiWiStringLiteral object) throws IOException {
        output.writeLong(object.getId());
        output.writeInt(object.getContent().length());
        output.writeChars(object.getContent());
        if(object.getLanguage() != null) {
            output.writeInt(object.getLanguage().length());
            output.writeChars(object.getLanguage());
        } else {
            output.writeInt(0);
        }

        output.writeObject(object.getDatatype());

        output.writeLong(object.getCreated().getTime());

    }

    @Override
    public KiWiStringLiteral readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        long id = input.readLong();
        int clen = input.readInt();
        char[] content = new char[clen];
        for(int i=0; i<clen; i++) {
            content[i]=input.readChar();
        }

        int llen = input.readInt();
        String lang = null;
        if(llen > 0) {
            char[] lb = new char[llen];
            for(int i=0; i<llen; i++) {
                lb[i] = input.readChar();
            }
            lang = new String(lb);
        }

        KiWiUriResource dtype = (KiWiUriResource) input.readObject();

        Date created = new Date(input.readLong());

        KiWiStringLiteral r = new KiWiStringLiteral(new String(content), lang != null ? Locale.forLanguageTag(lang) : null, dtype, created);
        r.setId(id);

        return r;
    }
}
