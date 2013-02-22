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

import fi.tikesos.rdfa.core.datatype.Component;
import fi.tikesos.rdfa.core.datatype.Language;
import fi.tikesos.rdfa.core.datatype.Literal;
import fi.tikesos.rdfa.core.exception.NullErrorHandler;
import fi.tikesos.rdfa.core.parser.RDFaParser;
import fi.tikesos.rdfa.core.parser.sax.SAXRDFaParser;
import fi.tikesos.rdfa.core.profile.Profile;
import fi.tikesos.rdfa.core.profile.ProfileHandler;
import fi.tikesos.rdfa.core.triple.TripleSink;
import fi.tikesos.rdfa.core.util.NullEntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom version of SimpleProfileHandler with a local cache of the most common profiles.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class RDFaProfileHandler implements ProfileHandler {


    private Map<String, Profile> profileCache = new HashMap<String, Profile>();

    public RDFaProfileHandler() {
    }

    /**
     * (non-Javadoc)
     *
     * @see fi.tikesos.rdfa.core.profile.ProfileHandler#loadProfile(java.lang.String)
     */
    @Override
    public Profile loadProfile(String profileURI) throws Exception {
        Profile profile = profileCache.get(profileURI);
        if (profile == null) {
            if(profileURI.equals(RDFaParser.XHTML_PROFILE)) {
                profile = new XHTMLProfile();
            } else if(profileURI.equals(RDFaParser.RDFA_PROFILE)) {
                profile = new RDFaProfile();
            } else {
                XMLReader reader = XMLReaderFactory.createXMLReader();
                ProfileTripleSink profileTripleSink = new ProfileTripleSink();
                SAXRDFaParser parser = new SAXRDFaParser(profileURI,
                        profileTripleSink, null, new NullErrorHandler(),
                        fi.tikesos.rdfa.core.parser.RDFaParser.XML_RDFA);
                // Disable validation
                reader.setFeature("http://xml.org/sax/features/validation",
                        Boolean.FALSE);
                // Set processor to return namespaces as attributes
                reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                        Boolean.TRUE);
                // Set content handler
                reader.setContentHandler(parser);
                // Set entity resolver
                reader.setEntityResolver(new NullEntityResolver());
                // Parse the file

                reader.parse(new InputSource(new URI(profileURI).toURL().openStream()));
                profile = new SimpleProfile(profileTripleSink.getTermMappings(),
                        profileTripleSink.getPrefixMappings(),
                        profileTripleSink.getDefaultVocabulary());
            }
            // Cache profile
            profileCache.put(profileURI, profile);
        }
        return profile;
    }

    /**
     * Profile implementation for SimpleProfileLoader
     *
     * @author ssakorho
     *
     */
    private class SimpleProfile implements Profile {
        private String defaultVocabulary;
        private Map<String, String> termMappings;
        private Map<String, String> prefixMappings;

        public SimpleProfile(Map<String, String> termMappings,
                             Map<String, String> prefixMappings, String defaultVocabulary) {
            this.termMappings = termMappings;
            this.prefixMappings = prefixMappings;
            this.defaultVocabulary = defaultVocabulary;
        }

        /**
         * (non-Javadoc)
         *
         * @see fi.tikesos.rdfa.core.profile.Profile#getTermMappings()
         */
        @Override
        public Map<String, String> getTermMappings() {
            return termMappings;
        }

        /**
         * (non-Javadoc)
         *
         * @see fi.tikesos.rdfa.core.profile.Profile#getPrefixMappings()
         */
        @Override
        public Map<String, String> getPrefixMappings() {
            return prefixMappings;
        }

        /**
         * (non-Javadoc)
         *
         * @see fi.tikesos.rdfa.core.profile.Profile#getDefaultVocabulary()
         */
        @Override
        public String getDefaultVocabulary() {
            return defaultVocabulary;
        }
    }

    /**
     * TripleSink implementation for SimpleProfileLoader
     *
     * @author ssakorho
     *
     */
    private class ProfileTripleSink implements TripleSink {
        private final static String RDFA_NS = "http://www.w3.org/ns/rdfa#";
        private String defaultVocabulary = null;
        private Map<String, Map<String, String>> tripleMap = new HashMap<String, Map<String, String>>();

        /**
         * @return list of term and uri pairs
         */
        public Map<String, String> getTermMappings() {
            Map<String, String> termMappings = new HashMap<String, String>();
            for (Map.Entry<String, Map<String, String>> tripleEntry : tripleMap
                    .entrySet()) {
                String term = tripleEntry.getValue().get("term");
                if (term != null && term.isEmpty() == false) {
                    String uri = tripleEntry.getValue().get("uri");
                    if (uri != null && uri.isEmpty() == false) {
                        termMappings.put(term, uri);
                    }
                }
            }
            return termMappings;
        }

        /**
         * @return the default vocabulary or null
         */
        public String getDefaultVocabulary() {
            return defaultVocabulary;
        }

        /**
         * @return list of prefix and uri pairs
         */
        public Map<String, String> getPrefixMappings() {
            Map<String, String> prefixMappings = new HashMap<String, String>();
            for (Map.Entry<String, Map<String, String>> tripleEntry : tripleMap
                    .entrySet()) {
                String prefix = tripleEntry.getValue().get("prefix");
                if (prefix != null && prefix.isEmpty() == false) {
                    String uri = tripleEntry.getValue().get("uri");
                    if (uri != null && uri.isEmpty() == false) {
                        prefixMappings.put(prefix, uri);
                    }
                }
            }
            return prefixMappings;
        }

        /**
         * (non-Javadoc)
         *
         * @see fi.tikesos.rdfa.core.triple.TripleSink#startRelativeTripleCaching()
         */
        @Override
        public void startRelativeTripleCaching() {
        }

        /**
         * (non-Javadoc)
         *
         * @see fi.tikesos.rdfa.core.triple.TripleSink#stopRelativeTripleCaching()
         */
        @Override
        public void stopRelativeTripleCaching() {
        }

        /**
         * (non-Javadoc)
         *
         */
        @Override
        public void generateTriple(Component subject, Component predicate,
                                   Component object) {
            // Ignored
        }

        /**
         * (non-Javadoc)
         *
         */
        @Override
        public void generateTripleLiteral(Component subject,
                                          Component predicate, Literal literal, Language language,
                                          Component datatype) {
            // Store triple to map, if it's of known type
            String type = null;
            if ((RDFA_NS + "uri").equals(predicate.getValue()) == true) {
                type = "uri";
            } else if ((RDFA_NS + "prefix").equals(predicate.getValue()) == true) {
                type = "prefix";
            } else if ((RDFA_NS + "term").equals(predicate.getValue()) == true) {
                type = "term";
            } else if ((RDFA_NS + "vocabulary").equals(predicate.getValue()) == true) {
                defaultVocabulary = literal.getValue();
            }
            if (type != null) {
                Map<String, String> valueMap = tripleMap
                        .get(subject.getValue());
                if (valueMap == null) {
                    valueMap = new HashMap<String, String>();
                    tripleMap.put(subject.getValue(), valueMap);
                }
                valueMap.put(type, literal.getValue());
            }
        }

        /**
         * (non-Javadoc)
         *
         * @see fi.tikesos.rdfa.core.triple.TripleSink#generateTriple(java.lang.String
         *      , java.lang.String, java.lang.String)
         */
        @Override
        public void generateTriple(String subject, String predicate,
                                   String object) {
            // NOT IMPLEMENTED
            throw new UnsupportedOperationException();
        }

        /**
         * (non-Javadoc)
         *
         */
        @Override
        public void generateTripleLiteral(String subject, String predicate,
                                          String lexical, String language, String datatype) {
            // NOT IMPLEMENTED
            throw new UnsupportedOperationException();
        }
    }

}
