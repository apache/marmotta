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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FN;

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



    public static final IRI SEARCH_FULLTEXT;

    public static final IRI QUERY_FULLTEXT;

    // IRIs for SPARQL built-in functions, used internally by marmotta
    public static final IRI RAND;
    public static final IRI UUID;
    public static final IRI STRUUID;
    public static final IRI NOW;
    public static final IRI YEAR;
    public static final IRI MONTH;
    public static final IRI DAY;
    public static final IRI HOURS;
    public static final IRI MINUTES;
    public static final IRI SECONDS;
    public static final IRI TIMEZONE;
    public static final IRI TZ;
    public static final IRI MD5;
    public static final IRI SHA1;
    public static final IRI SHA256;
    public static final IRI SHA384;
    public static final IRI SHA512;


    // statistics functions (supported by e.g. PostgreSQL)
    public static final IRI STDDEV;
    public static final IRI VARIANCE;


    static {
        ValueFactory f = SimpleValueFactory.getInstance();

        SEARCH_FULLTEXT = f.createIRI(NAMESPACE,"fulltext-search");
        QUERY_FULLTEXT = f.createIRI(NAMESPACE,"fulltext-query");

        RAND      = f.createIRI(NAMESPACE,"rand");
        UUID      = f.createIRI(NAMESPACE,"uuid");
        STRUUID   = f.createIRI(NAMESPACE,"struuid");
        NOW       = f.createIRI(NAMESPACE,"now");
        YEAR      = FN.YEAR_FROM_DATETIME;
        MONTH     = FN.MONTH_FROM_DATETIME;
        DAY       = FN.DAY_FROM_DATETIME;
        HOURS     = FN.HOURS_FROM_DATETIME;
        MINUTES   = FN.MINUTES_FROM_DATETIME;
        SECONDS   = FN.SECONDS_FROM_DATETIME;
        TIMEZONE  = FN.TIMEZONE_FROM_DATETIME;
        TZ        = f.createIRI(NAMESPACE,"tz");
        MD5       = f.createIRI(NAMESPACE,"md5");
        SHA1      = f.createIRI(NAMESPACE,"sha1");
        SHA256    = f.createIRI(NAMESPACE,"sha256");
        SHA384    = f.createIRI(NAMESPACE,"sha384");
        SHA512    = f.createIRI(NAMESPACE,"sha512");

        STDDEV    = f.createIRI(NAMESPACE,"stddev");
        VARIANCE  = f.createIRI(NAMESPACE,"variance");
    }
}
