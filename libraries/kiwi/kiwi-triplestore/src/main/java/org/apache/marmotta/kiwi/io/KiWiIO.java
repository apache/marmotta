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

package org.apache.marmotta.kiwi.io;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.io.DataIO;
import org.apache.marmotta.commons.vocabulary.SCHEMA;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.openrdf.model.vocabulary.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiIO {

    public static final String NS_DBPEDIA = "http://dbpedia.org/resource/";
    public static final String NS_FREEBASE = "http://rdf.freebase.com/ns/";
    private static Logger log = LoggerFactory.getLogger(KiWiIO.class);

    /**
     * Minimum length of content where we start using compression.
     */
    private static final int LITERAL_COMPRESS_LENGTH = 500;

    private static final int PREFIX_UNKNOWN = 0;
    private static final int PREFIX_XSD     = 1;
    private static final int PREFIX_RDF     = 2;
    private static final int PREFIX_RDFS    = 3;
    private static final int PREFIX_SKOS    = 4;
    private static final int PREFIX_DC      = 5;
    private static final int PREFIX_DCT     = 6;
    private static final int PREFIX_OWL     = 7;
    private static final int PREFIX_LOCAL   = 8;
    private static final int PREFIX_REDLINK = 9;
    private static final int PREFIX_SCHEMA  = 10;
    private static final int PREFIX_DBPEDIA = 11;
    private static final int PREFIX_FREEBASE= 12;


    private static final int TYPE_URI       = 1;
    private static final int TYPE_BNODE     = 2;
    private static final int TYPE_BOOLEAN   = 3;
    private static final int TYPE_DATE      = 4;
    private static final int TYPE_DOUBLE    = 5;
    private static final int TYPE_INT       = 6;
    private static final int TYPE_STRING    = 7;


    public static final int MODE_DEFAULT    = 1; // no compression
    public static final int MODE_PREFIX     = 2; // prefix compression for some known URI prefixes
    public static final int MODE_COMPRESSED = 3; // reserved: ZLIB string compression for long literals

    private static final int LANG_UNKNOWN = 0;
    private static final int LANG_EN = 1;
    private static final int LANG_DE = 2;
    private static final int LANG_FR = 3;
    private static final int LANG_ES = 4;
    private static final int LANG_NL = 5;
    private static final int LANG_PT = 6;
    private static final int LANG_RU = 7;
    private static final int LANG_SV = 8;
    private static final int LANG_NO = 9;
    private static final int LANG_FI = 10;
    private static final int LANG_DK = 11;
    private static final int LANG_IT = 12;
    private static final int LANG_PL = 13;


    public static final String HTTP_LOCALHOST = "http://localhost";
    public static final String NS_REDLINK = "http://data.redlink.io";


    private static Map<Class<? extends KiWiNode>, Integer> classTable = new HashMap<>();
    static {
        classTable.put(KiWiUriResource.class,    TYPE_URI);
        classTable.put(KiWiAnonResource.class,   TYPE_BNODE);
        classTable.put(KiWiBooleanLiteral.class, TYPE_BOOLEAN);
        classTable.put(KiWiDateLiteral.class,    TYPE_DATE);
        classTable.put(KiWiDoubleLiteral.class,  TYPE_DOUBLE);
        classTable.put(KiWiIntLiteral.class,     TYPE_INT);
        classTable.put(KiWiStringLiteral.class,  TYPE_STRING);
    }


    private static Map<String,Integer> langTable = new HashMap<>();
    static {
        langTable.put("en", LANG_EN);
        langTable.put("de", LANG_DE);
        langTable.put("fr", LANG_FR);
        langTable.put("es", LANG_ES);
        langTable.put("nl", LANG_NL);
        langTable.put("pt", LANG_PT);
        langTable.put("ru", LANG_RU);
        langTable.put("sv", LANG_SV);
        langTable.put("no", LANG_NO);
        langTable.put("fi", LANG_FI);
        langTable.put("dk", LANG_DK);
        langTable.put("it", LANG_IT);
        langTable.put("pl", LANG_PL);
    }

    /**
     * Efficiently serialize a KiWiNode to a DataOutput destination. The type of node will be encoded with a single
     * byte usinbg the TYPE_* constants defined in this class
     *
     * @param output  DataOutput destination
     * @param node  KiWiNode to serialize
     * @throws IOException
     */
    public static void writeNode(DataOutput output, KiWiNode node) throws IOException {
        if(node == null) {
            output.writeByte(0);
        } else {
            int type = classTable.get(node.getClass());
            output.writeByte(type);
            switch (type) {
                case TYPE_URI:
                    writeURI(output, (KiWiUriResource) node);
                    break;
                case TYPE_BNODE:
                    writeBNode(output, (KiWiAnonResource) node);
                    break;
                case TYPE_BOOLEAN:
                    writeBooleanLiteral(output, (KiWiBooleanLiteral) node);
                    break;
                case TYPE_DATE:
                    writeDateLiteral(output, (KiWiDateLiteral) node);
                    break;
                case TYPE_DOUBLE:
                    writeDoubleLiteral(output, (KiWiDoubleLiteral) node);
                    break;
                case TYPE_INT:
                    writeIntLiteral(output, (KiWiIntLiteral) node);
                    break;
                case TYPE_STRING:
                    writeStringLiteral(output, (KiWiStringLiteral) node);
                    break;
                default:
                    throw new IllegalArgumentException("unknown KiWiNode type: "+node.getClass());
            }
        }
    }


    /**
     * Read a KiWiNode serialized with writeNode and return it. The type indicator is used to determine which type
     * of resource to instantiate.
     *
     * @param input DataInput source
     * @return an instance of a subclass of KiWiNode, depending on the type indicator read from the source
     * @throws IOException
     */
    public static KiWiNode readNode(DataInput input) throws IOException {
        int type = input.readByte();
        switch (type) {
            case 0:
                return null;
            case TYPE_URI:
                return readURI(input);
            case TYPE_BNODE:
                return readBNode(input);
            case TYPE_BOOLEAN:
                return readBooleanLiteral(input);
            case TYPE_DATE:
                return readDateLiteral(input);
            case TYPE_DOUBLE:
                return readDoubleLiteral(input);
            case TYPE_INT:
                return readIntLiteral(input);
            case TYPE_STRING:
                return readStringLiteral(input);
            default:
                throw new IllegalArgumentException("unknown KiWiNode type: "+type);
        }

    }

    /**
     * Efficiently serialize a KiWiUriResource to a DataOutput destination, using prefix compression for commonly used
     * prefixes.
     *
     * @param out  DataOutput destination
     * @param uri  KiWiUriResource to serialize
     * @throws IOException
     */
    public static void writeURI(DataOutput out, KiWiUriResource uri) throws IOException {
        if(uri == null) {
            out.writeLong(-1L);
        } else {
            out.writeLong(uri.getId());

            // compression for commonly used constant prefixes
            if(uri.stringValue().startsWith(XSD.NAMESPACE)) {
                out.writeByte(PREFIX_XSD);
                DataIO.writeString(out, uri.stringValue().substring(XSD.NAMESPACE.length()));
            } else if(uri.stringValue().startsWith(RDF.NAMESPACE)) {
                out.writeByte(PREFIX_RDF);
                DataIO.writeString(out, uri.stringValue().substring(RDF.NAMESPACE.length()));
            } else if(uri.stringValue().startsWith(RDFS.NAMESPACE)) {
                out.writeByte(PREFIX_RDFS);
                DataIO.writeString(out, uri.stringValue().substring(RDFS.NAMESPACE.length()));
            } else if(uri.stringValue().startsWith(SKOS.NAMESPACE)) {
                out.writeByte(PREFIX_SKOS);
                DataIO.writeString(out, uri.stringValue().substring(SKOS.NAMESPACE.length()));
            } else if(uri.stringValue().startsWith(DC.NAMESPACE)) {
                out.writeByte(PREFIX_DC);
                DataIO.writeString(out, uri.stringValue().substring(DC.NAMESPACE.length()));
            } else if(uri.stringValue().startsWith(DCTERMS.NAMESPACE)) {
                out.writeByte(PREFIX_DCT);
                DataIO.writeString(out, uri.stringValue().substring(DCTERMS.NAMESPACE.length()));
            } else if(uri.stringValue().startsWith(OWL.NAMESPACE)) {
                out.writeByte(PREFIX_OWL);
                DataIO.writeString(out, uri.stringValue().substring(OWL.NAMESPACE.length()));
            } else if(uri.stringValue().startsWith(SCHEMA.NAMESPACE)) {
                out.writeByte(PREFIX_SCHEMA);
                DataIO.writeString(out, uri.stringValue().substring(SCHEMA.NAMESPACE.length()));
            } else if(uri.stringValue().startsWith(NS_REDLINK)) {
                out.writeByte(PREFIX_REDLINK);
                DataIO.writeString(out, uri.stringValue().substring(NS_REDLINK.length()));
            } else if(uri.stringValue().startsWith(NS_DBPEDIA)) {
                out.writeByte(PREFIX_DBPEDIA);
                DataIO.writeString(out, uri.stringValue().substring(NS_DBPEDIA.length()));
            } else if(uri.stringValue().startsWith(NS_FREEBASE)) {
                out.writeByte(PREFIX_FREEBASE);
                DataIO.writeString(out, uri.stringValue().substring(NS_FREEBASE.length()));
            } else if(uri.stringValue().startsWith(HTTP_LOCALHOST)) {
                out.writeByte(PREFIX_LOCAL);
                DataIO.writeString(out, uri.stringValue().substring(HTTP_LOCALHOST.length()));
            } else {
                out.writeByte(PREFIX_UNKNOWN);
                DataIO.writeString(out, uri.stringValue());
            }

            out.writeLong(uri.getCreated().getTime());
        }
    }


    /**
     * Read a KiWiUriResource serialized with writeURI and return it.
     *
     * @param input DataInput source
     * @return a KiWiUriResource
     * @throws IOException
     */
    public static KiWiUriResource readURI(DataInput input) throws IOException {
        long id = input.readLong();

        if(id == -1) {
            return null;
        } else {


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
                case PREFIX_SCHEMA:
                    uriPrefix = SCHEMA.NAMESPACE;
                    break;
                case PREFIX_REDLINK:
                    uriPrefix = NS_REDLINK;
                    break;
                case PREFIX_DBPEDIA:
                    uriPrefix = NS_DBPEDIA;
                    break;
                case PREFIX_FREEBASE:
                    uriPrefix = NS_FREEBASE;
                    break;
                case PREFIX_LOCAL:
                    uriPrefix = HTTP_LOCALHOST;
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
    }


    /**
     * Efficiently serialize a KiWiAnonResource to a DataOutput destination.
     *
     * @param out the destination
     * @param bnode the KiWiAnonResource to serialize
     * @throws IOException
     */
    public static void writeBNode(DataOutput out, KiWiAnonResource bnode) throws IOException {
        if(bnode == null) {
            out.writeLong(-1L);
        } else {
            out.writeLong(bnode.getId());
            DataIO.writeString(out, bnode.stringValue());
            out.writeLong(bnode.getCreated().getTime());
        }
    }

    /**
     * Read a KiWiAnonResource serialized with writeBNode from a DataInput source
     *
     * @param input the source
     * @return the de-serialized KiWiAnonResource
     * @throws IOException
     */
    public static KiWiAnonResource readBNode(DataInput input) throws IOException {
        long id = input.readLong();

        if(id == -1) {
            return null;
        } else {
            String anonId = DataIO.readString(input);

            Date created = new Date(input.readLong());

            KiWiAnonResource r = new KiWiAnonResource(anonId,created);
            r.setId(id);

            return r;
        }
    }


    /**
     * Efficiently serialize a KiWiBooleanLiteral to a DataOutput destination.
     *
     * @param out the destination
     * @param literal the KiWiBooleanLiteral to serialize
     * @throws IOException
     */
    public static void writeBooleanLiteral(DataOutput out, KiWiBooleanLiteral literal) throws IOException {
        if(literal == null) {
            out.writeLong(-1L);
        } else {
            out.writeLong(literal.getId());
            out.writeBoolean(literal.booleanValue());
            writeURI(out, literal.getType());
            out.writeLong(literal.getCreated().getTime());
        }
    }


    /**
     * Read a KiWiBooleanLiteral serialized with writeBooleanLiteral from a DataInput source
     *
     * @param input the source
     * @return the de-serialized KiWiBooleanLiteral
     * @throws IOException
     */
    public static KiWiBooleanLiteral readBooleanLiteral(DataInput input) throws IOException {
        long id = input.readLong();

        if(id == -1) {
            return null;
        } else {
            boolean content = input.readBoolean();

            KiWiUriResource dtype = readURI(input);

            Date created = new Date(input.readLong());

            KiWiBooleanLiteral r = new KiWiBooleanLiteral(content, dtype, created);
            r.setId(id);

            return r;
        }
    }


    /**
     * Efficiently serialize a KiWiDateLiteral to a DataOutput destination.
     *
     * @param out the destination
     * @param literal the KiWiDateLiteral to serialize
     * @throws IOException
     */
    public static void writeDateLiteral(DataOutput out, KiWiDateLiteral literal) throws IOException {
        if(literal == null) {
            out.writeLong(-1L);
        } else {
            out.writeLong(literal.getId());
            out.writeLong(literal.getDateContent().getMillis());
            out.writeInt(literal.getDateContent().getZone().getOffset(literal.getDateContent()));
            writeURI(out, literal.getType());
            out.writeLong(literal.getCreated().getTime());
        }
    }


    /**
     * Read a KiWiDateLiteral serialized with writeDateLiteral from a DataInput source
     *
     * @param input the source
     * @return the de-serialized KiWiDateLiteral
     * @throws IOException
     */
    public static KiWiDateLiteral readDateLiteral(DataInput input) throws IOException {
        long id = input.readLong();

        if(id == -1) {
            return null;
        } else {
            DateTime content = new DateTime(input.readLong(), DateTimeZone.forOffsetMillis(input.readInt()));

            KiWiUriResource dtype = readURI(input);

            Date created = new Date(input.readLong());

            KiWiDateLiteral r = new KiWiDateLiteral(content, dtype, created);
            r.setId(id);

            return r;
        }
    }


    /**
     * Efficiently serialize a KiWiDoubleLiteral to a DataOutput destination.
     *
     * @param out the destination
     * @param literal the KiWiDoubleLiteral to serialize
     * @throws IOException
     */
    public static void writeDoubleLiteral(DataOutput out, KiWiDoubleLiteral literal) throws IOException {
        if(literal == null) {
            out.writeLong(-1L);
        } else {
            out.writeLong(literal.getId());
            out.writeDouble(literal.getDoubleContent());
            writeURI(out, literal.getType());
            out.writeLong(literal.getCreated().getTime());
        }
    }


    /**
     * Read a KiWiDoubleLiteral serialized with writeDoubleLiteral from a DataInput source
     *
     * @param input the source
     * @return the de-serialized KiWiDoubleLiteral
     * @throws IOException
     */
    public static KiWiDoubleLiteral readDoubleLiteral(DataInput input) throws IOException {
        long id = input.readLong();

        if(id == -1) {
            return null;
        } else {
            double content = input.readDouble();

            KiWiUriResource dtype = readURI(input);

            Date created = new Date(input.readLong());

            KiWiDoubleLiteral r = new KiWiDoubleLiteral(content, dtype, created);
            r.setId(id);

            return r;
        }
    }


    /**
     * Efficiently serialize a KiWiIntLiteral to a DataOutput destination.
     *
     * @param out the destination
     * @param literal the KiWiIntLiteral to serialize
     * @throws IOException
     */
    public static void writeIntLiteral(DataOutput out, KiWiIntLiteral literal) throws IOException {
        if(literal == null) {
            out.writeLong(-1L);
        } else {
            out.writeLong(literal.getId());
            out.writeLong(literal.getIntContent());
            writeURI(out, literal.getType());
            out.writeLong(literal.getCreated().getTime());
        }
    }


    /**
     * Read a KiWiIntLiteral serialized with writeIntLiteral from a DataInput source
     *
     * @param input the source
     * @return the de-serialized KiWiIntLiteral
     * @throws IOException
     */
    public static KiWiIntLiteral readIntLiteral(DataInput input) throws IOException {
        long id = input.readLong();

        if(id == -1) {
            return null;
        } else {
            long content = input.readLong();

            KiWiUriResource dtype = readURI(input);

            Date created = new Date(input.readLong());

            KiWiIntLiteral r = new KiWiIntLiteral(content, dtype, created);
            r.setId(id);

            return r;
        }
    }



    /**
     * Efficiently serialize a KiWiStringLiteral to a DataOutput destination.
     *
     * @param out the destination
     * @param literal the KiWiStringLiteral to serialize
     * @throws IOException
     */
    public static void writeStringLiteral(DataOutput out, KiWiStringLiteral literal) throws IOException {
        if(literal == null) {
            out.writeLong(-1L);
        } else {
            out.writeLong(literal.getId());
            writeContent(out, literal.getContent());
            if(langTable.containsKey(literal.getLanguage())) {
                out.writeByte(langTable.get(literal.getLanguage()));
            } else {
                out.writeByte(LANG_UNKNOWN);
                DataIO.writeString(out, literal.getLanguage());
            }
            writeURI(out, literal.getType());
            out.writeLong(literal.getCreated().getTime());
        }
    }



    /**
     * Read a KiWiStringLiteral serialized with writeStringLiteral from a DataInput source
     *
     * @param input the source
     * @return the de-serialized KiWiStringLiteral
     * @throws IOException
     */
    public static KiWiStringLiteral readStringLiteral(DataInput input) throws IOException {
        long id = input.readLong();

        if(id == -1) {
            return null;
        } else {
            String content = readContent(input);
            byte   langB   = input.readByte();
            String lang;

            switch (langB) {
                case LANG_EN:
                    lang = "en";
                    break;
                case LANG_DE:
                    lang = "de";
                    break;
                case LANG_FR:
                    lang = "fr";
                    break;
                case LANG_ES:
                    lang = "es";
                    break;
                case LANG_IT:
                    lang = "it";
                    break;
                case LANG_PT:
                    lang = "pt";
                    break;
                case LANG_NL:
                    lang = "nl";
                    break;
                case LANG_SV:
                    lang = "sv";
                    break;
                case LANG_NO:
                    lang = "no";
                    break;
                case LANG_FI:
                    lang = "fi";
                    break;
                case LANG_RU:
                    lang = "ru";
                    break;
                case LANG_DK:
                    lang = "dk";
                    break;
                case LANG_PL:
                    lang = "pl";
                    break;
                default:
                    lang = DataIO.readString(input);
            }



            KiWiUriResource dtype = readURI(input);

            Date created = new Date(input.readLong());

            KiWiStringLiteral r = new KiWiStringLiteral(content, lang != null ? Locale.forLanguageTag(lang) : null, dtype, created);
            r.setId(id);

            return r;
        }
    }


    /**
     * Efficiently serialize a KiWiTriple to a DataOutput destination.
     *
     * @param output the destination
     * @param triple the KiWiTriple to serialize
     * @throws IOException
     */
    public static void writeTriple(DataOutput output, KiWiTriple triple) throws IOException {
        output.writeLong(triple.getId());

        // in case subject and object are both uris we use a special prefix-compressed mode
        if(triple.getSubject().isUriResource() && triple.getObject().isUriResource()) {
            String sUri = triple.getSubject().stringValue();
            String oUri = triple.getObject().stringValue();

            String prefix = StringUtils.getCommonPrefix(sUri, oUri);

            output.writeByte(MODE_PREFIX);
            DataIO.writeString(output,prefix);

            output.writeLong(triple.getSubject().getId());
            DataIO.writeString(output, sUri.substring(prefix.length()));
            output.writeLong(triple.getSubject().getCreated().getTime());

            writeURI(output,triple.getPredicate());

            output.writeLong(triple.getObject().getId());
            DataIO.writeString(output, oUri.substring(prefix.length()));
            output.writeLong(triple.getObject().getCreated().getTime());
        } else {
            output.writeByte(MODE_DEFAULT);

            writeNode(output,triple.getSubject());
            writeURI(output,triple.getPredicate());
            writeNode(output,triple.getObject());

        }

        writeNode(output,triple.getContext());
        writeNode(output,triple.getCreator());
        output.writeBoolean(triple.isDeleted());
        output.writeBoolean(triple.isInferred());
        output.writeBoolean(triple.isNewTriple());
        output.writeLong(triple.getCreated().getTime());
        if(triple.getDeletedAt() != null) {
            output.writeLong(triple.getDeletedAt().getTime());
        } else {
            output.writeLong(0);
        }
    }


    /**
     * Read a KiWiTriple serialized with writeTriple from a DataInput source
     *
     * @param input the source
     * @return the de-serialized KiWiTriple
     * @throws IOException
     */
    public static KiWiTriple readTriple(DataInput input) throws IOException {
        KiWiTriple result = new KiWiTriple();
        result.setId(input.readLong());

        int mode = input.readByte();
        if(mode == MODE_PREFIX) {
            String prefix = DataIO.readString(input);

            long sId = input.readLong();
            String sUri = prefix + DataIO.readString(input);
            long sTime = input.readLong();
            KiWiUriResource s = new KiWiUriResource(sUri);
            s.setId(sId);
            s.setCreated(new Date(sTime));
            result.setSubject(s);

            result.setPredicate(readURI(input));

            long oId = input.readLong();
            String oUri = prefix + DataIO.readString(input);
            long oTime = input.readLong();
            KiWiUriResource o = new KiWiUriResource(oUri);
            o.setId(oId);
            o.setCreated(new Date(oTime));
            result.setObject(o);

        } else {
            result.setSubject((KiWiResource) readNode(input));
            result.setPredicate(readURI(input));
            result.setObject(readNode(input));
        }
        result.setContext((KiWiResource) readNode(input));
        result.setCreator((KiWiResource) readNode(input));
        result.setDeleted(input.readBoolean());
        result.setInferred(input.readBoolean());
        result.setNewTriple(input.readBoolean());

        result.setCreated(new Date(input.readLong()));

        long deletedAt = input.readLong();
        if(deletedAt > 0) {
            result.setDeletedAt(new Date(deletedAt));
        }


        return result;

    }

    /**
     * Read a potentially compressed string from the data input.
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static String readContent(DataInput in) throws IOException {
        int mode = in.readByte();

        if(mode == MODE_COMPRESSED) {
            try {
                int strlen = in.readInt();
                int buflen = in.readInt();

                byte[] buffer = new byte[buflen];
                in.readFully(buffer);

                Inflater decompressor = new Inflater(true);
                decompressor.setInput(buffer);

                byte[] data = new byte[strlen];
                decompressor.inflate(data);
                decompressor.end();

                return new String(data,"UTF-8");
            } catch(DataFormatException ex) {
                throw new IllegalStateException("input data is not valid",ex);
            }
        } else {
            return DataIO.readString(in);
        }
    }

    /**
     * Write a string to the data output. In case the string length exceeds LITERAL_COMPRESS_LENGTH, uses a LZW
     * compressed format, otherwise writes the plain bytes.
     *
     * @param out      output destination to write to
     * @param content  string to write
     * @throws IOException
     */
    private static void writeContent(DataOutput out, String content) throws IOException {
        if(content.length() > LITERAL_COMPRESS_LENGTH) {
            // temporary buffer of the size of bytes in the content string (assuming that the compressed data will fit into it)
            byte[] data   = content.getBytes("UTF-8");
            byte[] buffer = new byte[data.length];

            Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION, true);
            compressor.setInput(data);
            compressor.finish();

            int length = compressor.deflate(buffer);

            // only use compressed version if it is smaller than the number of bytes used by the string
            if(length < buffer.length) {
                log.debug("compressed string with {} bytes; compression ratio {}", data.length, (double) length / data.length);

                out.writeByte(MODE_COMPRESSED);
                out.writeInt(data.length);
                out.writeInt(length);
                out.write(buffer,0,length);
            } else {
                log.warn("compressed length exceeds string buffer: {} > {}", length, buffer.length);

                out.writeByte(MODE_DEFAULT);
                DataIO.writeString(out,content);
            }

            compressor.end();
        } else {
            out.writeByte(MODE_DEFAULT);
            DataIO.writeString(out,content);
        }
    }
}
