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
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.SyModule;
import com.sun.syndication.feed.rss.Category;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Enclosure;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;

import org.apache.marmotta.commons.sesame.rio.rss.RSSFormat;
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

import java.io.*;
import java.net.URLEncoder;

/**
 * Parse RSS feed into RDF. Uses the following vocabularies:
 * <ul>
 *     <li>dcterms for representing most metadata about feeds and entries</li>
 *     <li>sioc for type information and relation between concepts</li>
 *     <li>skos for representing categories associated with items or channels</li>
 *     <li>media ontology for representing information from the mediarss extension</li>
 *     <li>wgs84 geo ontology for representing information from the georss extension</li>
 * </ul>
 * RSS properties without a good corresponding vocabulary are copied 1:1 using the rss namespace itself.
 * <p/>
 * Author: Sebastian Schaffert
 */
public final class RSSParser extends FeedParserBase {

    private static Logger log = LoggerFactory.getLogger(RSSParser.class);


    /**
     * Creates a new RDFParserBase that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create RDF model objects.
     */
    public RSSParser() {
        this(new ValueFactoryImpl());
    }

    /**
     * Creates a new RDFParserBase that will use the supplied ValueFactory to
     * create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    public RSSParser(ValueFactory valueFactory) {
        super(valueFactory);
        this.valueFactory = valueFactory;
    }



    /**
     * Gets the RDF format that this parser can parse.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RSSFormat.FORMAT;
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
            if(feed instanceof Channel) {
                parseFeed((Channel) feed);
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
            if(feed instanceof Channel) {
                parseFeed((Channel) feed);
            } else {
                throw new RDFParseException("data stream is not an RSS feed");
            }
        } catch (FeedException e) {
            throw new RDFParseException(e);
        }
    }



    private void parseFeedEntry(final Item entry, final Resource r_feed) throws RDFParseException, RDFHandlerException {

        final String entryURI = entry.getUri() != null ? entry.getUri() : entry.getLink();

        URI r_entry = createURI(entryURI);
        URI rdf_type = createURI(NS_RDF + "type");


        // add type sioc:Post
        rdfHandler.handleStatement(createStatement(r_entry, rdf_type, createURI(NS_SIOC + "Post")));

        // add as sioc:container_of from parent feed
        rdfHandler.handleStatement(createStatement(r_feed, createURI(NS_SIOC + "container_of"), r_entry));
        rdfHandler.handleStatement(createStatement(r_entry, createURI(NS_SIOC + "has_container"), r_feed));

        createStringProperty(r_entry, NS_DC_TERMS + "creator", entry.getAuthor());

        for(Object category : entry.getCategories()) {
            parseCategory(r_entry, (Category)category);
        }

        createUrlProperty(r_entry, NS_SIOC + "has_discussion", entry.getComments());

        if(entry.getContent() != null) {
            createStringProperty(r_entry, NS_RSS_CONTENT + "encoded", entry.getContent().getValue());
            createStringProperty(r_entry, NS_RSS_CONTENT + "format", entry.getContent().getType());
        }

        if(entry.getDescription() != null) {
            createStringProperty(r_entry, NS_DC_TERMS + "description", entry.getDescription().getValue());
        }

        // enclosures relate items to media resources used; we use dcterms:hasPart to link to them
        for(Enclosure enclosure : entry.getEnclosures()) {
            createUrlProperty(r_entry, NS_DC_TERMS + "hasPart", enclosure.getUrl());
        }

        // for the expiration date we use dc:valid; it is a bit underspecified :-(
        createDateProperty(r_entry, NS_DC_TERMS + "valid", entry.getExpirationDate());

        // GUID is sometimes a URL but the documentation says this cannot be guaranteed, so we use dc:identifier
        createStringProperty(r_entry, NS_DC_TERMS + "identifier", entry.getGuid().getValue());

        // for the link we use sioc:link
        createUrlProperty(r_entry, NS_SIOC + "link", entry.getLink());

        for(Module module : entry.getModules()) {
            if(module instanceof DCModule) {
                parseDCModule(r_entry, (DCModule)module);
            } else if(module instanceof GeoRSSModule) {
                parseGeoModule(r_entry, (GeoRSSModule)module);
            } else if(module instanceof MediaEntryModule) {
                parseMediaModule(r_entry, (MediaEntryModule)module);
            } else if(module instanceof ContentModule) {
                parseContentModule(r_entry, (ContentModule)module);
            } else {
                log.warn("module {} not supported yet", module.getUri());
            }

            // TODO: add support for more modules!
        }

        // publication date is dc:issued
        createDateProperty(r_entry, NS_DC_TERMS + "issued", entry.getPubDate());

        // if the source is present, we link just to the URL using dc:source and ignore the text
        if(entry.getSource() != null)
            createUrlProperty(r_entry, NS_DC_TERMS + "source", entry.getSource().getUrl());

        // title is dc:title
        createStringProperty(r_entry, NS_DC_TERMS + "title", entry.getTitle());

        log.debug("parsed RSS item {}", r_entry.stringValue());
    }

    /**
     * Import data from an RSS or atom feed using the ROME SyndFeed representation.
     *
     * @param feed the ROME rss/atom feed representation
     * @return count of imported documents
     */
    private void parseFeed(final Channel feed) throws RDFParseException, RDFHandlerException {
        if (log.isInfoEnabled()) {
            log.info("importing entries from {} feed '{}' found at '{}'",new Object[] {feed.getFeedType(),feed.getTitle(),feed.getUri()});
        }

        final String feedUri = feed.getUri() != null ? feed.getUri() : feed.getLink();
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

        // add all categories that are present
        for(Category category : feed.getCategories()) {
            parseCategory(r_feed,category);
        }

        // if feed.getCloud() present, we add its specifications using the RSS namespace
        if(feed.getCloud() != null) {
            createStringProperty(r_feed, NS_RSS + "cloudUpdateProtocol", feed.getCloud().getProtocol());
            createStringProperty(r_feed, NS_RSS + "cloudUpdateDomain", feed.getCloud().getDomain());
            createStringProperty(r_feed, NS_RSS + "cloudUpdatePath", feed.getCloud().getPath());
            createStringProperty(r_feed, NS_RSS + "cloudUpdateProcedure", feed.getCloud().getRegisterProcedure());
            createIntProperty(r_feed, NS_RSS + "cloudUpdatePort", feed.getCloud().getPort());
        }

        // add dc:rights for feed.getCopyright()
        createStringProperty(r_feed, NS_DC_TERMS + "rights", feed.getCopyright());

        // add dc:description for feed.getDescription()
        createStringProperty(r_feed, NS_DC_TERMS + "description", feed.getDescription());

        // ignore feed.getDocs()

        // add dc:creator to point to the software used for generating feed
        createStringProperty(r_feed, NS_DC_TERMS + "provenance", feed.getGenerator());

        // add foaf:depiction in case there is an image
        if(feed.getImage() != null)
            createUrlProperty(r_feed, NS_FOAF + "depiction", feed.getImage().getUrl());

        // add all feed items
        for(Item item : feed.getItems()) {
            parseFeedEntry(item, r_feed);
        }

        // add dc:language for feed.getLanguage()
        createStringProperty(r_feed, NS_DC_TERMS + "language", feed.getLanguage());

        // add dc:created for getLastBuildDate()
        createDateProperty(r_feed, NS_DC_TERMS + "created", feed.getLastBuildDate());

        // add sioc:link for getLink()
        createUrlProperty(r_feed, NS_SIOC + "link", feed.getLink());

        // add dc:creator for managing editor
        createStringProperty(r_feed, NS_DC_TERMS + "creator", feed.getManagingEditor());

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

        // create publication date as dc:issued
        createDateProperty(r_feed, NS_DC_TERMS+"issued",feed.getPubDate());

        // PICS is superseded and there is no proper RDF way to do it, so we use an RSS property
        createStringProperty(r_feed, NS_RSS + "rating", feed.getRating());

        // skip days are also added using RSS vocabulary, they are actually syndication info
        for(String day : feed.getSkipDays()) {
            createStringProperty(r_feed, NS_RSS + "skipDay", day);
        }
        for(Integer hour : feed.getSkipHours()) {
            createIntProperty(r_feed, NS_RSS + "skipHour", hour);
        }

        // textinput: we skip it, the documentation says:
        // "The purpose of the <textInput> element is something of a mystery. You can use it to specify a
        // search engine box. Or to allow a reader to provide feedback. Most aggregators ignore it. "

        createStringProperty(r_feed, NS_DC_TERMS + "title", feed.getTitle());

        // ttl is again meta information about the syndication, we use the RSS namespace
        if(feed.getTtl() > 0)
            createIntProperty(r_feed, NS_RSS + "ttl", feed.getTtl());

        // add dc:publisher for webmaster
        createStringProperty(r_feed, NS_DC_TERMS + "publisher", feed.getWebMaster());

        log.info("importing RSS feed finished successfully.");
    }

    protected void parseCategory(Resource resource, Category category) throws RDFHandlerException, RDFParseException {
        if(category.getValue() == null) {
            return;
        }

        try {
            Resource skosConcept;
            if(category.getDomain() != null && category.getValue() != null) {
                // create a skos:Concept with the domain as namespace and a local name derived from the value, add it as sioc:topic
                String localName = URLEncoder.encode(category.getValue(), "UTF-8");
                String namespace = category.getDomain();
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


}
