/**
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
package org.apache.marmotta.commons.sesame.rio.rss;

import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.module.DCSubject;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import org.rometools.feed.module.content.ContentModule;
import org.rometools.feed.module.georss.GeoRSSModule;
import org.rometools.feed.module.mediarss.MediaEntryModule;
import org.rometools.feed.module.mediarss.types.MediaContent;
import org.rometools.feed.module.mediarss.types.Metadata;
import org.rometools.feed.module.mediarss.types.UrlReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Common functionality for RSS and Atom feed parsing
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class FeedParserBase extends RDFParserBase {
    private static Logger log = LoggerFactory.getLogger(FeedParserBase.class);


    protected static final String NS_RDF                      = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    protected static final String NS_DC                       = "http://purl.org/dc/elements/1.1/";
    protected static final String NS_DC_TERMS                 = "http://purl.org/dc/terms/";
    protected static final String NS_SIOC                     = "http://rdfs.org/sioc/ns#";
    protected static final String NS_SKOS                     = "http://www.w3.org/2004/02/skos/core#";
    protected static final String NS_RSS                      = "http://purl.org/rss/1.0/";
    protected static final String NS_RSS_CONTENT              = "http://purl.org/rss/1.0/modules/content/";
    protected static final String NS_RSS_SY                   = "http://purl.org/rss/1.0/modules/syndication/";
    protected static final String NS_ADMIN                    = "http://webns.net/mvcb/";
    protected static final String NS_FOAF                     = "http://xmlns.com/foaf/0.1/";
    protected static final String NS_GEO                      = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    protected static final String NS_MA                       = "http://www.w3.org/ns/ma-ont#";


    protected ValueFactory valueFactory;


    /**
     * Creates a new RDFParserBase that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create RDF model objects.
     */
    protected FeedParserBase() {
        this(new ValueFactoryImpl());
    }

    /**
     * Creates a new RDFParserBase that will use the supplied ValueFactory to
     * create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    protected FeedParserBase(ValueFactory valueFactory) {
        super(valueFactory);
        this.valueFactory = valueFactory;
    }

    @Override
    public void setValueFactory(ValueFactory valueFactory) {
        super.setValueFactory(valueFactory);
        this.valueFactory = valueFactory;
    }


    protected void parseDCModule(Resource resource, DCModule dcModule) throws RDFHandlerException, RDFParseException {
        for(String contributor : dcModule.getContributors()) {
            createStringProperty(resource, NS_DC_TERMS + "contributor", contributor);
        }
        for(String coverage : dcModule.getCoverages()) {
            createStringProperty(resource, NS_DC_TERMS + "coverage", coverage);
        }
        for(String creator : dcModule.getCreators()) {
            createStringProperty(resource, NS_DC_TERMS + "creator", creator);
        }
        for(Date date : dcModule.getDates()) {
            createDateProperty(resource, NS_DC_TERMS + "date", date);
        }
        for(String description : dcModule.getDescriptions()) {
            createStringProperty(resource, NS_DC_TERMS + "description", description);
        }
        for(String format : dcModule.getFormats()) {
            createStringProperty(resource, NS_DC_TERMS + "format", format);
        }
        for(String identifier : dcModule.getIdentifiers()) {
            createStringProperty(resource, NS_DC_TERMS + "identifier", identifier);
        }
        for(String language : dcModule.getLanguages()) {
            createStringProperty(resource, NS_DC_TERMS + "language", language);
        }
        for(String publisher : dcModule.getPublishers()) {
            createStringProperty(resource, NS_DC_TERMS + "publisher", publisher);
        }
        for(String relation : dcModule.getRelations()) {
            createUrlProperty(resource, NS_DC_TERMS + "relation", relation);
        }
        for(String rights : dcModule.getRightsList()) {
            createStringProperty(resource, NS_DC_TERMS + "rights", rights);
        }
        for(String source : dcModule.getSources()) {
            createUrlProperty(resource, NS_DC_TERMS + "source", source);
        }
        for(DCSubject subject : dcModule.getSubjects()) {
            parseDCSubject(resource, subject);
        }
        for(String title : dcModule.getTitles()) {
            createStringProperty(resource, NS_DC_TERMS + "title", title);
        }
        for(String type : dcModule.getTypes()) {
            createStringProperty(resource, NS_DC_TERMS + "type", type);
        }
    }

    protected void parseContentModule(Resource resource, ContentModule contentModule) throws RDFHandlerException, RDFParseException {
        for(Object content : contentModule.getEncodeds()) {
            createStringProperty(resource,NS_RSS_CONTENT + "encoded",(String)content);
        }

        // TODO: more sophisticated forms are nowadays rarely used, we do not support them
        if(contentModule.getContentItems() != null && contentModule.getContentItems().size() > 0) {
            log.warn("content items are not supported yet");
        }
    }

    protected void parseGeoModule(Resource resource, GeoRSSModule geoRSSModule) throws RDFParseException, RDFHandlerException {
        if(geoRSSModule.getPosition() != null) {
            Resource r_location = createBNode();
            Resource t_adr = createURI(NS_GEO + "Point");
            URI p_type     = createURI(NS_RDF + "type");
            rdfHandler.handleStatement(createStatement(r_location,p_type,t_adr));

            createDoubleProperty(r_location,NS_GEO+"latitude",geoRSSModule.getPosition().getLatitude());
            createDoubleProperty(r_location,NS_GEO+"longitude",geoRSSModule.getPosition().getLongitude());


            rdfHandler.handleStatement(createStatement(resource,createURI(NS_DC_TERMS + "spatial"),r_location));

        }
    }

    protected void parseMediaModule(Resource resource, MediaEntryModule mediaEntryModule) throws RDFParseException, RDFHandlerException {
        for(MediaContent content : mediaEntryModule.getMediaContents()) {
            if(content.getReference() != null && content.getReference() instanceof UrlReference) {
                URI r_media = createURI(((UrlReference) content.getReference()).getUrl().toString());
                rdfHandler.handleStatement(createStatement(r_media, createURI(NS_RDF + "type"), createURI(NS_MA + "MediaResource")));
                rdfHandler.handleStatement(createStatement(r_media, createURI(NS_MA + "locator"), r_media));

                if(content.getBitrate() != null)
                    createDoubleProperty(r_media, NS_MA + "averageBitRate", content.getBitrate());
                if(content.getDuration() != null)
                    createLongProperty(r_media, NS_MA + "duration", content.getDuration());

                createStringProperty(r_media, NS_MA + "hasFormat", content.getType());

                if(content.getFramerate() != null)
                    createDoubleProperty(r_media, NS_MA + "frameRate", content.getFramerate());

                if(content.getHeight() != null)
                    createIntProperty(r_media, NS_MA + "frameHeight", content.getHeight());
                if(content.getWidth() != null)
                    createIntProperty(r_media, NS_MA + "frameWidth", content.getWidth());

                createStringProperty(r_media, NS_MA + "hasLanguage", content.getLanguage());

                if(content.getMetadata() != null) {
                    Metadata metadata = content.getMetadata();

                    createStringProperty(r_media, NS_MA + "title", metadata.getTitle());
                    createStringProperty(r_media, NS_MA + "copyright", metadata.getCopyright());
                    createStringProperty(r_media, NS_MA + "description", metadata.getDescription());

                    for(String keyword : metadata.getKeywords()) {
                        createStringProperty(r_media, NS_MA + "hasKeyword", keyword);
                    }

                }

                rdfHandler.handleStatement(createStatement(resource, createURI(NS_SIOC+"hasPart"), r_media));
            }
        }

    }



    protected void parseDCSubject(Resource resource, DCSubject category) throws RDFHandlerException, RDFParseException {
        if(category.getValue() == null) {
            return;
        }

        try {
            Resource skosConcept;
            if(category.getTaxonomyUri() != null && category.getValue() != null) {
                // create a skos:Concept with the domain as namespace and a local name derived from the value, add it as sioc:topic
                String localName = URLEncoder.encode(category.getValue(),"UTF-8");
                String namespace = category.getTaxonomyUri();
                skosConcept = createURI(namespace+(namespace.endsWith("/") || namespace.endsWith("#")?"":"/")+localName);
            } else  {
                // create a skos:Concept with the baseUri as namespace and a local name derived from the value, add it as sioc:topic
                String localName = URLEncoder.encode(category.getValue(), "UTF-8");
                skosConcept = resolveURI(localName);
            }
            createUrlProperty(skosConcept,NS_RDF + "type", NS_SKOS+"Concept");
            createStringProperty(skosConcept, NS_SKOS + "prefLabel", category.getValue());
            rdfHandler.handleStatement(createStatement(resource,createURI(NS_SIOC + "topic"),skosConcept));
        } catch (UnsupportedEncodingException e) {
            throw new RDFParseException(e);
        }


        // add category value as dc:subject
        createStringProperty(resource, NS_DC_TERMS + "subject", category.getValue());

    }


    protected void createStringProperty(Resource resource, String rdfProperty, String value) throws RDFParseException, RDFHandlerException {
        if(value != null && !"".equals(value.trim())) {
            URI p_description = createURI(rdfProperty);
            Literal v_description = createLiteral(value, null, null);
            rdfHandler.handleStatement(createStatement(resource,p_description,v_description));
        }
    }

    protected void createIntProperty(Resource resource, String rdfProperty, int value) throws RDFParseException, RDFHandlerException {
        URI p_description = createURI(rdfProperty);
        Literal v_description = createLiteral(""+value, null, createURI("http://www.w3.org/2001/XMLSchema#int"));
        rdfHandler.handleStatement(createStatement(resource,p_description,v_description));
    }

    protected void createLongProperty(Resource resource, String rdfProperty, long value) throws RDFParseException, RDFHandlerException {
        URI p_description = createURI(rdfProperty);
        Literal v_description = createLiteral(""+value, null, createURI("http://www.w3.org/2001/XMLSchema#long"));
        rdfHandler.handleStatement(createStatement(resource,p_description,v_description));
    }

    protected void createDoubleProperty(Resource resource, String rdfProperty, double value) throws RDFParseException, RDFHandlerException {
        URI p_description = createURI(rdfProperty);
        Literal v_description = createLiteral(""+value, null, createURI("http://www.w3.org/2001/XMLSchema#double"));
        rdfHandler.handleStatement(createStatement(resource,p_description,v_description));
    }


    protected void createDateProperty(Resource resource, String rdfProperty, Date value) throws RDFParseException, RDFHandlerException {
        if(value != null) {
            URI p_dateprop = createURI(rdfProperty);
            Literal v_dateprop = valueFactory.createLiteral(getXMLCalendar(value,null));
            rdfHandler.handleStatement(createStatement(resource,p_dateprop,v_dateprop));
        }
    }


    protected void createUrlProperty(Resource resource, String rdfProperty, String value) throws RDFParseException, RDFHandlerException {
        if(value != null) {
            URI p_description = createURI(rdfProperty);
            URI v_description = createURI(value);
            rdfHandler.handleStatement(createStatement(resource,p_description,v_description));
        }
    }

    protected void createUrlProperty(Resource resource, String rdfProperty, Resource value) throws RDFParseException, RDFHandlerException {
        if(value != null) {
            URI p_description = createURI(rdfProperty);
            rdfHandler.handleStatement(createStatement(resource,p_description,value));
        }
    }


    protected static XMLGregorianCalendar getXMLCalendar(Date date, TimeZone timezone) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        if(timezone != null)
            c.setTimeZone(timezone);
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            return null;
        }
    }



}
