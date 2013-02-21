/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.commons.sesame.rio.rdfa;

import com.google.common.collect.ImmutableMap;
import fi.tikesos.rdfa.core.profile.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class XHTMLProfile implements Profile {
    public static final String NS = "http://www.w3.org/1999/xhtml/vocab#";

    private static Map<String,String> termMappings = new HashMap<String, String>();
    static {
        termMappings.put("alternate", NS+"alternate");
        termMappings.put("appendix", NS+"appendix");
        termMappings.put("bookmark", NS+"bookmark");
        termMappings.put("cite", NS+"cite");
        termMappings.put("chapter", NS+"chapter");
        termMappings.put("contents", NS+"contents");
        termMappings.put("copyright", NS+"copyright");
        termMappings.put("first", NS+"first");
        termMappings.put("glossary", NS+"glossary");
        termMappings.put("help", NS+"help");
        termMappings.put("icon", NS+"icon");
        termMappings.put("index", NS+"index");
        termMappings.put("itsRules", NS+"itsRules");
        termMappings.put("last", NS+"last");
        termMappings.put("license", NS+"license");
        termMappings.put("meta", NS+"meta");
        termMappings.put("next", NS+"next");
        termMappings.put("p3pv1", NS+"p3pv1");
        termMappings.put("prev", NS+"prev");
        termMappings.put("role", NS+"role");
        termMappings.put("section", NS+"section");
        termMappings.put("stylesheet", NS+"stylesheet");
        termMappings.put("subsection", NS+"subsection");
        termMappings.put("start", NS+"start");
        termMappings.put("top", NS+"top");
        termMappings.put("up", NS+"up");
    }


    private static Map<String,String> prefixMappings = new HashMap<String, String>();
    static {
        // official
        prefixMappings.put("grddl","http://www.w3.org/2003/g/data-view#");
        prefixMappings.put("ma",   "http://www.w3.org/ns/ma-ont#");
        prefixMappings.put("owl",  "http://www.w3.org/2002/07/owl#");
        prefixMappings.put("rdf",  "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prefixMappings.put("rdfa", "http://www.w3.org/ns/rdfa#");
        prefixMappings.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prefixMappings.put("rif",  "http://www.w3.org/2007/rif#");
        prefixMappings.put("skos", "http://www.w3.org/2004/02/skos/core#");
        prefixMappings.put("skosxl","http://www.w3.org/2008/05/skos-xl#");
        prefixMappings.put("wdr",  "http://www.w3.org/2007/05/powder#");
        prefixMappings.put("void", "http://rdfs.org/ns/void#");
        prefixMappings.put("wdrs", "http://www.w3.org/2007/05/powder-s#");
        prefixMappings.put("xhv",  "http://www.w3.org/1999/xhtml/vocab#");
        prefixMappings.put("xml",  "http://www.w3.org/XML/1998/namespace");
        prefixMappings.put("xsd", "http://www.w3.org/2001/XMLSchema#");

        // in-progress

        // commonly used
        prefixMappings.put("cc",   "http://creativecommons.org/ns#");
        prefixMappings.put("ctag", "http://commontag.org/ns#");
        prefixMappings.put("dc",   "http://purl.org/dc/elements/1.1/");
        prefixMappings.put("dcterms","http://purl.org/dc/terms/");
        prefixMappings.put("foaf", "http://xmlns.com/foaf/0.1/");
        prefixMappings.put("gr",   "http://purl.org/goodrelations/v1#");
        prefixMappings.put("ical",  "http://www.w3.org/2002/12/cal/icaltzd#");
        prefixMappings.put("og",    "http://ogp.me/ns#");
        prefixMappings.put("rev",   "http://purl.org/stuff/rev#");
        prefixMappings.put("sioc",  "http://rdfs.org/sioc/ns#");
        prefixMappings.put("v",     "http://rdf.data-vocabulary.org/#");
        prefixMappings.put("vcard", "http://www.w3.org/2006/vcard/ns#");
        prefixMappings.put("schema","http://schema.org/");

    }


    /**
     * @return Term mappings defined in the profile
     */
    @Override
    public Map<String, String> getTermMappings() {
        return ImmutableMap.copyOf(termMappings);
    }

    /**
     * @return Prefix mappings defined in the profile
     */
    @Override
    public Map<String, String> getPrefixMappings() {
        return ImmutableMap.copyOf(prefixMappings);
    }

    /**
     * @return Default vocabulary set in the profile or null
     */
    @Override
    public String getDefaultVocabulary() {
        return null;
    }
}
