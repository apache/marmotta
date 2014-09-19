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
package org.apache.marmotta.kiwi.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FN;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class FN_MARMOTTA {

    public static final String NAMESPACE = "http://marmotta.apache.org/vocabulary/sparql-functions#";

    /**
     * Recommended prefix for the XPath Functions namespace: "fn"
     */
    public static final String PREFIX = "mm";

    /**
     * An immutable {@link org.openrdf.model.Namespace} constant that represents the XPath
     * Functions namespace.
     */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);



    public static final URI SEARCH_FULLTEXT;

    public static final URI QUERY_FULLTEXT;

    // URIs for SPARQL built-in functions, used internally by marmotta
    public static final URI RAND;
    public static final URI UUID;
    public static final URI STRUUID;
    public static final URI NOW;
    public static final URI YEAR;
    public static final URI MONTH;
    public static final URI DAY;
    public static final URI HOURS;
    public static final URI MINUTES;
    public static final URI SECONDS;
    public static final URI TIMEZONE;
    public static final URI TZ;
    public static final URI MD5;
    public static final URI SHA1;
    public static final URI SHA256;
    public static final URI SHA384;
    public static final URI SHA512;


    // statistics functions (supported by e.g. PostgreSQL)
    public static final URI STDDEV;
    public static final URI VARIANCE;


    static {
        ValueFactory f = new ValueFactoryImpl();

        SEARCH_FULLTEXT = f.createURI(NAMESPACE,"fulltext-search");
        QUERY_FULLTEXT = f.createURI(NAMESPACE,"fulltext-query");

        RAND      = f.createURI(NAMESPACE,"rand");
        UUID      = f.createURI(NAMESPACE,"uuid");
        STRUUID   = f.createURI(NAMESPACE,"struuid");
        NOW       = f.createURI(NAMESPACE,"now");
        YEAR      = FN.YEAR_FROM_DATETIME;
        MONTH     = FN.MONTH_FROM_DATETIME;
        DAY       = FN.DAY_FROM_DATETIME;
        HOURS     = FN.HOURS_FROM_DATETIME;
        MINUTES   = FN.MINUTES_FROM_DATETIME;
        SECONDS   = FN.SECONDS_FROM_DATETIME;
        TIMEZONE  = FN.TIMEZONE_FROM_DATETIME;
        TZ        = f.createURI(NAMESPACE,"tz");
        MD5       = f.createURI(NAMESPACE,"md5");
        SHA1      = f.createURI(NAMESPACE,"sha1");
        SHA256    = f.createURI(NAMESPACE,"sha256");
        SHA384    = f.createURI(NAMESPACE,"sha384");
        SHA512    = f.createURI(NAMESPACE,"sha512");

        STDDEV    = f.createURI(NAMESPACE,"stddev");
        VARIANCE  = f.createURI(NAMESPACE,"variance");
    }
}
