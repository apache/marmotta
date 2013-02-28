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

import com.google.common.base.Preconditions;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.*;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.SyModule;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;

import org.apache.marmotta.commons.sesame.rio.rss.AtomFormat;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.rometools.feed.module.content.ContentModule;
import org.rometools.feed.module.georss.GeoRSSModule;
import org.rometools.feed.module.mediarss.MediaEntryModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Parse Atom feed into RDF. Uses the following vocabularies:
 * <ul>
 *     <li>dcterms for representing most metadata about feeds and entries</li>
 *     <li>sioc for type information and relation between concepts</li>
 *     <li>skos for representing categories associated with items or channels</li>
 *     <li>media ontology for representing information from the mediarss extension</li>
 *     <li>wgs84 geo ontology for representing information from the georss extension</li>
 * </ul>
 * <p/>
 * Author: Sebastian Schaffert
 */
public class AtomParser extends FeedParserBase {


    private static Logger log = LoggerFactory.getLogger(AtomParser.class);


    /**
     * Creates a new RDFParserBase that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create RDF model objects.
     */
    public AtomParser() {
        this(new ValueFactoryImpl());
    }

    /**
     * Creates a new RDFParserBase that will use the supplied ValueFactory to
     * create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    public AtomParser(ValueFactory valueFactory) {
        super(valueFactory);
        this.valueFactory = valueFactory;
    }



    /**
     * Gets the RDF format that this parser can parse.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return AtomFormat.FORMAT;
    }


    /**
     * Parses the data from the supplied InputStream, using the supplied baseURI
     * to resolve any relative URI references.
     *
     * @param in      The InputStream from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                             If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                             If the configured statement handler has encountered an
     *                             unrecoverable error.
     */
    @Override
    public void parse(InputStream in, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        Preconditions.checkNotNull(baseURI);

        setBaseURI(baseURI);

        WireFeedInput input = new WireFeedInput();
        try {
            WireFeed feed = input.build(new InputSource(in));
            if(feed instanceof Feed) {
                parseFeed((Feed) feed);
            } else {
                throw new RDFParseException("data stream is not an RSS feed");
            }
        } catch (FeedException e) {
            throw new RDFParseException(e);
        }
    }

    /**
     * Parses the data from the supplied Reader, using the supplied baseURI to
     * resolve any relative URI references.
     *
     * @param reader  The Reader from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                             If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                             If the configured statement handler has encountered an
     *                             unrecoverable error.
     */
    @Override
    public void parse(Reader reader, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        Preconditions.checkNotNull(baseURI);

        setBaseURI(baseURI);

        WireFeedInput input = new WireFeedInput();
        try {
            WireFeed feed = input.build(reader);
            if(feed instanceof Feed) {
                parseFeed((Feed) feed);
            } else {
                throw new RDFParseException("data stream is not an RSS feed");
            }
        } catch (FeedException e) {
            throw new RDFParseException(e);
        }
    }



    private void parseFeedEntry(final Entry entry, final Resource r_feed) throws RDFParseException, RDFHandlerException {

        final String entryURI = entry.getId();

        URI r_entry = createURI(entryURI);
        URI rdf_type = createURI(NS_RDF + "type");


        // add type sioc:Post
        rdfHandler.handleStatement(createStatement(r_entry, rdf_type, createURI(NS_SIOC + "Post")));

        // add as sioc:container_of from parent feed
        rdfHandler.handleStatement(createStatement(r_feed, createURI(NS_SIOC + "container_of"), r_entry));
        rdfHandler.handleStatement(createStatement(r_entry, createURI(NS_SIOC + "has_container"), r_feed));

        // for each link we create a sioc:link
        for(Link link : entry.getAlternateLinks()) {
            createUrlProperty(r_entry,NS_SIOC + "link", resolveURI(link.getHref()));
        }

        // add all authors as dc:creator
        for(Person person : entry.getAuthors()) {
            parsePerson(r_entry, person, "creator");
        }

        for(Object category : entry.getCategories()) {
            parseCategory(r_entry, (Category)category);
        }

        for(Content content : entry.getContents()) {
            createStringProperty(r_entry, NS_RSS_CONTENT + "encoded", content.getValue());
            createStringProperty(r_entry, NS_RSS_CONTENT + "format", content.getType());
            createStringProperty(r_entry, NS_DC_TERMS + "description",content.getValue());
        }

        // add all authors as dc:creator
        for(Person person : entry.getContributors()) {
            parsePerson(r_entry, person, "contributor");
        }

        createDateProperty(r_entry, NS_DC_TERMS + "created", entry.getCreated());

        // ignore foreign markup

        createDateProperty(r_entry, NS_DC_TERMS + "issued", entry.getPublished());
        createDateProperty(r_entry, NS_DC_TERMS + "modified", entry.getUpdated());


        // GUID is sometimes a URL but the documentation says this cannot be guaranteed, so we use dc:identifier
        createStringProperty(r_entry, NS_DC_TERMS + "identifier", entry.getId());


        for(Object module : entry.getModules()) {
            if(module instanceof DCModule) {
                parseDCModule(r_entry, (DCModule)module);
            } else if(module instanceof GeoRSSModule) {
                parseGeoModule(r_entry, (GeoRSSModule)module);
            } else if(module instanceof MediaEntryModule) {
                parseMediaModule(r_entry, (MediaEntryModule)module);
            } else if(module instanceof ContentModule) {
                parseContentModule(r_entry, (ContentModule)module);
            } else {
                log.warn("module {} not supported yet", module.toString());
            }

            // TODO: add support for more modules!
        }

        // for each link we create a sioc:link
        for(Link link : entry.getOtherLinks()) {
            createUrlProperty(r_entry,NS_SIOC + "link", resolveURI(link.getHref()));
        }

        // copyright information
        createStringProperty(r_entry, NS_DC_TERMS + "rights", entry.getRights());

        // if the source is present, we link just to the URL using dc:source and ignore the text
        if(entry.getSource() != null) {
            createUrlProperty(r_entry, NS_DC_TERMS + "source", entry.getSource().getId());
        }

        if(entry.getSummary() != null) {
            createStringProperty(r_entry, NS_DC_TERMS + "abstract",entry.getSummary().getValue());
        }

        // title is dc:title
        createStringProperty(r_entry, NS_DC_TERMS + "title", entry.getTitle());

        log.debug("parsed Atom item {}", r_entry.stringValue());
    }

    /**
     * Import data from an RSS or atom feed using the ROME SyndFeed representation.
     *
     * @param feed the ROME rss/atom feed representation
     * @return count of imported documents
     */
    private void parseFeed(final Feed feed) throws RDFParseException, RDFHandlerException {
        if (log.isInfoEnabled()) {
            log.info("importing entries from {} feed '{}' found at '{}'",new Object[] {feed.getFeedType(),feed.getTitle(),feed.getId()});
        }

        final String feedUri = feed.getId();
        if (feedUri == null) {
            log.error("feed '{}' has neither uri nor link to reference", feed.getTitle());
            return;
        }

        // we set some namespaces first
        setNamespace(NS_DC_TERMS,"dcterms");
        setNamespace(NS_RSS_SY,"sy");
        setNamespace(NS_RSS_CONTENT,"content");
        setNamespace(NS_SIOC,"sioc");

        URI r_feed = createURI(feedUri);
        URI rdf_type = createURI(NS_RDF + "type");

        // add type sioc:Forum
        rdfHandler.handleStatement(createStatement(r_feed, rdf_type, createURI(NS_SIOC + "Forum")));
        createUrlProperty(r_feed, NS_SIOC + "feed", feedUri);

        // for each link we create a sioc:link
        for(Link link : feed.getAlternateLinks()) {
            createUrlProperty(r_feed,NS_SIOC + "link", resolveURI(link.getHref()));
        }

        // add all authors as dc:creator
        for(Person person : feed.getAuthors()) {
            parsePerson(r_feed, person, "creator");
        }


        // add all categories that are present
        for(Object category : feed.getCategories()) {
            parseCategory(r_feed, (Category) category);
        }

        // add all contributors as dc:contributor
        for(Person person : feed.getAuthors()) {
            parsePerson(r_feed, person, "contributor");
        }


        // add dc:creator to point to the software used for generating feed
        createStringProperty(r_feed, NS_DC_TERMS + "provenance", feed.getGenerator().getValue());

        // add foaf:depiction in case there is an image
        if(feed.getIcon() != null) {
            createUrlProperty(r_feed, NS_FOAF + "thumbnail", resolveURI(feed.getIcon()));
        }

        // add all feed items
        for(Entry item : feed.getEntries()) {
            parseFeedEntry(item, r_feed);
        }

        // add dc:language for feed.getLanguage()
        createStringProperty(r_feed, NS_DC_TERMS + "language", feed.getLanguage());

        // add foaf:depiction in case there is an image
        if(feed.getLogo() != null) {
            createUrlProperty(r_feed, NS_FOAF + "logo", resolveURI(feed.getLogo()));
        }



        for(Module module : feed.getModules()) {
            if(module instanceof SyModule) {
                SyModule syModule = (SyModule)module;
                createStringProperty(r_feed,NS_RSS_SY + "updatePeriod", syModule.getUpdatePeriod());
                createIntProperty(r_feed, NS_RSS_SY + "updateFrequency", syModule.getUpdateFrequency());
                createDateProperty(r_feed, NS_RSS_SY + "updateBase", syModule.getUpdateBase());
            } else if(module instanceof DCModule) {
                parseDCModule(r_feed, (DCModule)module);
            }
        }

        // for each link we create a sioc:link
        for(Link link : feed.getOtherLinks()) {
            createUrlProperty(r_feed,NS_SIOC + "link", resolveURI(link.getHref()));
        }
        // add dc:rights for feed.getCopyright()
        createStringProperty(r_feed, NS_DC_TERMS + "rights", feed.getRights());

        // add dc:description for feed.getDescription()
        if(feed.getSubtitle() != null) {
            createStringProperty(r_feed, NS_DC_TERMS + "description", feed.getSubtitle().getValue());
        }


        // textinput: we skip it, the documentation says:
        // "The purpose of the <textInput> element is something of a mystery. You can use it to specify a
        // search engine box. Or to allow a reader to provide feedback. Most aggregators ignore it. "

        createStringProperty(r_feed, NS_DC_TERMS + "title", feed.getTitle());

        // add dc:created and dc:issued for update date
        createDateProperty(r_feed, NS_DC_TERMS + "created", feed.getUpdated());
        createDateProperty(r_feed, NS_DC_TERMS + "issued", feed.getUpdated());

        log.info("importing Atom feed finished successfully.");
    }

    protected void parseCategory(Resource resource, Category category) throws RDFHandlerException, RDFParseException {
        if(category.getTerm() == null) {
            return;
        }

        try {
            Resource skosConcept;
            if(category.getScheme() != null ) {
                // create a skos:Concept with the domain as namespace and a local name derived from the value, add it as sioc:topic
                String localName = URLEncoder.encode(category.getTerm(), "UTF-8");
                String namespace = category.getScheme();
                skosConcept = createURI(namespace+(namespace.endsWith("/") || namespace.endsWith("#")?"":"/")+localName);
            } else  {
                // create a skos:Concept with the baseUri as namespace and a local name derived from the value, add it as sioc:topic
                String localName = URLEncoder.encode(category.getTerm(), "UTF-8");
                skosConcept = resolveURI(localName);
            }
            createUrlProperty(skosConcept,NS_RDF + "type", NS_SKOS+"Concept");
            if(category.getLabel() != null) {
                createStringProperty(skosConcept, NS_SKOS + "prefLabel", category.getLabel());
            } else {
                createStringProperty(skosConcept, NS_SKOS + "prefLabel", category.getTerm());
            }
            rdfHandler.handleStatement(createStatement(resource,createURI(NS_SIOC + "topic"),skosConcept));
        } catch (UnsupportedEncodingException e) {
            throw new RDFParseException(e);
        }


        // add category value as dc:subject
        if(category.getLabel() != null) {
            createStringProperty(resource, NS_DC_TERMS + "subject", category.getLabel());
        } else {
            createStringProperty(resource, NS_DC_TERMS + "subject", category.getTerm());
        }

    }


    protected void parsePerson(Resource r_entry, Person person, String relation) throws RDFParseException, RDFHandlerException {
        if("creator".equals(relation) && (person.getUri() != null || person.getEmail() != null)) {
            String personUri = person.getUri() != null ? person.getUri() : "mailto:"+person.getEmail();
            Resource r_person = createURI(personUri);
            createStringProperty(r_person, NS_FOAF + "name", person.getName());
            if(person.getEmail() != null) {
                createUrlProperty(r_person, NS_FOAF + "mbox", "mailto:"+person.getEmail());
            }
            createUrlProperty(r_person, NS_FOAF + "homepage", person.getUri());

            rdfHandler.handleStatement(createStatement(r_entry, createURI(NS_FOAF + "maker"), r_person));
            rdfHandler.handleStatement(createStatement(r_person, createURI(NS_FOAF + "made"), r_entry));
        }
        createStringProperty(r_entry,NS_DC_TERMS + relation,person.getName());
    }

}
