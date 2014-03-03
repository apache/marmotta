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
import org.apache.marmotta.commons.io.DataIO;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.openrdf.model.vocabulary.*;

import java.io.IOException;
import java.util.Date;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class UriSerializer implements StreamSerializer<KiWiUriResource> {

    private static final int PREFIX_UNKNOWN = 0;
    private static final int PREFIX_XSD     = 1;
    private static final int PREFIX_RDF     = 2;
    private static final int PREFIX_RDFS    = 3;
    private static final int PREFIX_SKOS    = 4;
    private static final int PREFIX_DC      = 5;
    private static final int PREFIX_DCT     = 6;
    private static final int PREFIX_OWL     = 7;


    @Override
    public int getTypeId() {
        return ExternalizerIds.URI;
    }

    @Override
    public void write(ObjectDataOutput output, KiWiUriResource object) throws IOException {
        output.writeLong(object.getId());

        // compression for commonly used constant prefixes
        if(object.stringValue().startsWith(XSD.NAMESPACE)) {
            output.writeByte(PREFIX_XSD);
            DataIO.writeString(output, object.stringValue().substring(XSD.NAMESPACE.length()));
        } else if(object.stringValue().startsWith(RDF.NAMESPACE)) {
            output.writeByte(PREFIX_RDF);
            DataIO.writeString(output, object.stringValue().substring(RDF.NAMESPACE.length()));
        } else if(object.stringValue().startsWith(RDFS.NAMESPACE)) {
            output.writeByte(PREFIX_RDFS);
            DataIO.writeString(output, object.stringValue().substring(RDFS.NAMESPACE.length()));
        } else if(object.stringValue().startsWith(SKOS.NAMESPACE)) {
            output.writeByte(PREFIX_SKOS);
            DataIO.writeString(output, object.stringValue().substring(SKOS.NAMESPACE.length()));
        } else if(object.stringValue().startsWith(DC.NAMESPACE)) {
            output.writeByte(PREFIX_DC);
            DataIO.writeString(output, object.stringValue().substring(DC.NAMESPACE.length()));
        } else if(object.stringValue().startsWith(DCTERMS.NAMESPACE)) {
            output.writeByte(PREFIX_DCT);
            DataIO.writeString(output, object.stringValue().substring(DCTERMS.NAMESPACE.length()));
        } else if(object.stringValue().startsWith(OWL.NAMESPACE)) {
            output.writeByte(PREFIX_OWL);
            DataIO.writeString(output, object.stringValue().substring(OWL.NAMESPACE.length()));
        } else {
            output.writeByte(PREFIX_UNKNOWN);
            DataIO.writeString(output, object.stringValue());
        }

        output.writeLong(object.getCreated().getTime());
    }

    @Override
    public KiWiUriResource read(ObjectDataInput input) throws IOException {
        long id = input.readLong();

        int prefixMode = input.readByte();
        String uriPrefix = "";
        String uriSuffix = DataIO.readString(input);

        switch (prefixMode) {
            case PREFIX_XSD:
                uriPrefix = XSD.NAMESPACE;
                break;
            case PREFIX_RDF:
                uriPrefix = RDF.NAMESPACE;
                break;
            case PREFIX_RDFS:
                uriPrefix = RDFS.NAMESPACE;
                break;
            case PREFIX_SKOS:
                uriPrefix = SKOS.NAMESPACE;
                break;
            case PREFIX_DC:
                uriPrefix = DC.NAMESPACE;
                break;
            case PREFIX_DCT:
                uriPrefix = DCTERMS.NAMESPACE;
                break;
            case PREFIX_OWL:
                uriPrefix = OWL.NAMESPACE;
                break;
            default:
                uriPrefix = "";
                break;
        }

        Date created = new Date(input.readLong());

        KiWiUriResource r = new KiWiUriResource(uriPrefix + uriSuffix,created);
        r.setId(id);

        return r;
    }

    @Override
    public void destroy() {

    }
}
