/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.commons.sesame.model;

/**
 * Namespaces static constants
 * 
 * @author Sebastian Schaffert
 * @todo see how it fits with the PrefixService
 */
public class Namespaces {

    // the KiWi namespace used in XHTML documents for kiwi: attributes and elements
    public static final String NS_KIWI_HTML                = "http://www.kiwi-project.eu/kiwi/html/";
    public static final String NS_KIWI_EXPORT              = "http://www.kiwi-project.eu/kiwi/export/";

    public static final String NS_KIWI_CORE                = "http://www.kiwi-project.eu/kiwi/core/";
    public static final String NS_KIWI_REASONING           = "http://www.kiwi-project.eu/kiwi/reasoning/";
    public static final String NS_KIWI_CORE_KNOWLEDGESPACE = "http://www.kiwi-project.eu/kiwi/core/knowledgespace#";
    public static final String NS_KIWI_SPECIAL             = "http://www.kiwi-project.eu/kiwi/special/";
    public static final String NS_KIWI_TRIPLE              = "http://www.kiwi-project.eu/kiwi/triple/";
    public static final String NS_TAGIT                    = "http://www.kiwi-project.eu/tagit/";
    public static final String NS_EVENTIM                  = "http://www.eventim.de/";

    public static final String NS_FCP_CORE                 = "http://www.newmedialab.at/fcp/";

    public static final String NS_DEMO                     = "http://www.lmf-demo.at/";

    // LMF Namespaces
    public static final String NS_LMF_TYPES                = "http://www.newmedialab.at/lmf/types/1.0/";
    public static final String NS_LMF_FUNCS                = "http://www.newmedialab.at/lmf/functions/1.0/";

    // XML Namespaces
    public static final String NS_XSD                      = "http://www.w3.org/2001/XMLSchema#";
    public static final String NS_XML                      = "http://www.w3.org/TR/2006/REC-xml11-20060816/#";
    public static final String NS_XHTML                    = "http://www.w3.org/1999/xhtml";

    // RDF Namespaces
    public static final String NS_RDF                      = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String NS_RDFS                     = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String NS_OWL                      = "http://www.w3.org/2002/07/owl#";

    public static final String NS_ATOM                     = "http://www.w3.org/2005/Atom";

    // namespaces of some frequently used ontologies
    public static final String NS_FOAF                     = "http://xmlns.com/foaf/0.1/";
    public static final String NS_LASTFM                   = "http://foaf.qdos.com/lastfm/schema/";
    public static final String NS_BIO                      = "http://purl.org/vocab/bio/0.1/";
    public static final String NS_CONT                     = "http://www.w3.org/2000/10/swap/pim/contact#";
    public static final String NS_GEO                      = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    public static final String NS_GEONAMES                 = "http://www.geonames.org/ontology#";
    public static final String NS_SIOC                     = "http://rdfs.org/sioc/ns#";
    public static final String NS_SIOC_TYPES               = "http://rdfs.org/sioc/types#";
    public static final String NS_MOAT                     = "http://moat-project.org/ns#";
    public static final String NS_HGTAGS                   = "http://www.holygoat.co.uk/owl/redwood/0.1/tags/";
    public static final String NS_DC                       = "http://purl.org/dc/elements/1.1/";
    public static final String NS_DC_TERMS                 = "http://purl.org/dc/terms/";
    public static final String NS_SKOS                     = "http://www.w3.org/2004/02/skos/core#";
    public static final String NS_SCOT                     = "http://scot-project.org/scot/ns#";
    public static final String NS_EXIF                     = "http://www.kanzaki.com/ns/exif#";
    public static final String NS_MEDIA                    = "http://www.w3.org/ns/ma-ont#";
    public static final String NS_TEMPLATING			   = "http://newmedialab.at/onthology/templating/";

    public static final String NS_MEDIA_ANALYSIS		   = "http://linkedmultimedia.org/ontology/";

    public static final String NS_RSS                      = "http://purl.org/rss/1.0/";
    public static final String NS_RSS_CONTENT              = "http://purl.org/rss/1.0/modules/content/";

    /**
     * Some language related namespaces from LoC (Library of Congress, USA)
     */
    public static final class NSS_LANGUAGE {
        private NSS_LANGUAGE() {}

        /**
         * Use the two-letter identifier, such as "en", "de", "fr"
         */
        public static final String NS_ISO639_1 = "http://id.loc.gov/vocabulary/iso639-1/";
        /**
         * Use the three-letter identifier, such as "eng", "deu", "fra"
         */
        public static final String NS_ISO639_2 = "http://id.loc.gov/vocabulary/iso639-2/";
        /**
         * Language Families
         */
        public static final String NS_ISO639_5 = "http://id.loc.gov/vocabulary/iso639-5/";
        /**
         * MARC List for Languages.
         * MARC List for Languages provides three-character lowercase alphabetic strings that serve
         * as the identifiers of languages and language groups.
         * 
         * @see {@link http://id.loc.gov/vocabulary/languages.html}
         */
        public static final String NS_MARC     = "http://id.loc.gov/vocabulary/languages/";
    }

    public static final String MIME_TYPE_ALL               = "*/*";
    public static final String MIME_TYPE_HTML              = "text/html";
    public static final String MIME_TYPE_PLAIN             = "text/plain";
    public static final String MIME_TYPE_TEXT_XML          = "text/xml";
    public static final String MIME_TYPE_JSON              = "application/json";
    public static final String MIME_TYPE_XHTML             = "application/xhtml+xml";
    public static final String MIME_TYPE_RDFXML            = "application/rdf+xml";
    public static final String MIME_TYPE_XML               = "application/xml";
    public static final String MIME_TYPE_FORM_URLENC       = "application/x-www-form-urlencoded";
    public static final String MIME_TYPE_TEMPLATE		   = "text/html";

    /**
     * The login for the admin user
     */
    public static final String ADMIN_LOGIN = "admin";
    /**
     * The login for the anonymous user
     */
    public static final String ANONYMOUS_LOGIN = "anonymous";
}
