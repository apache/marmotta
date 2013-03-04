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
package org.apache.marmotta.commons.sesame.rio.vcard;

import com.google.common.base.Preconditions;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.parameter.Type;
import net.fortuna.ical4j.vcard.property.*;

import org.apache.marmotta.commons.sesame.rio.vcard.VCardFormat;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A parser for parsing VCard files into RDF. Uses the vcard vocabulary (http://www.w3.org/2006/vcard/ns) for
 * representing properties.
 * <p/>
 * Ontology documentation: http://www.w3.org/2006/vcard/ns-2006.html#Name
 * <p/>
 * Author: Sebastian Schaffert
 */
public class VCardParser extends RDFParserBase {

    private static Logger log = LoggerFactory.getLogger(VCardParser.class);

    public static final String NS_RDF    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String NS_VCARD  = "http://www.w3.org/2006/vcard/ns#";

    private static Map<Property.Id, String> propertyMappings = new HashMap<Property.Id, String>();
    static {
        propertyMappings.put(Property.Id.ADR,      "http://www.w3.org/2006/vcard/ns#adr");
        propertyMappings.put(Property.Id.AGENT,    "http://www.w3.org/2006/vcard/ns#agent");
        propertyMappings.put(Property.Id.BDAY,     "http://www.w3.org/2006/vcard/ns#bday");
        propertyMappings.put(Property.Id.CATEGORIES, "http://www.w3.org/2006/vcard/ns#category");
        propertyMappings.put(Property.Id.CLASS,    "http://www.w3.org/2006/vcard/ns#class");
        propertyMappings.put(Property.Id.EMAIL,    "http://www.w3.org/2006/vcard/ns#email");
        propertyMappings.put(Property.Id.EXTENDED, "http://www.w3.org/2006/vcard/ns#extended-address");
        propertyMappings.put(Property.Id.FN,       "http://www.w3.org/2006/vcard/ns#fn");
        propertyMappings.put(Property.Id.GEO,      "http://www.w3.org/2006/vcard/ns#geo");
        propertyMappings.put(Property.Id.KEY,      "http://www.w3.org/2006/vcard/ns#key");
        propertyMappings.put(Property.Id.LABEL,    "http://www.w3.org/2006/vcard/ns#label");
        propertyMappings.put(Property.Id.LOGO,     "http://www.w3.org/2006/vcard/ns#logo");
        propertyMappings.put(Property.Id.N,        "http://www.w3.org/2006/vcard/ns#n");
        propertyMappings.put(Property.Id.NICKNAME, "http://www.w3.org/2006/vcard/ns#nickname");
        propertyMappings.put(Property.Id.NOTE,     "http://www.w3.org/2006/vcard/ns#note");
        propertyMappings.put(Property.Id.ORG,      "http://www.w3.org/2006/vcard/ns#org");
        propertyMappings.put(Property.Id.PHOTO,    "http://www.w3.org/2006/vcard/ns#photo");
        propertyMappings.put(Property.Id.PRODID,   "http://www.w3.org/2006/vcard/ns#prodid");
        propertyMappings.put(Property.Id.REV,      "http://www.w3.org/2006/vcard/ns#rev");
        propertyMappings.put(Property.Id.ROLE,     "http://www.w3.org/2006/vcard/ns#role");
        propertyMappings.put(Property.Id.SORT_STRING,"http://www.w3.org/2006/vcard/ns#sort-string");
        propertyMappings.put(Property.Id.SOUND,    "http://www.w3.org/2006/vcard/ns#sound");
        propertyMappings.put(Property.Id.TEL,      "http://www.w3.org/2006/vcard/ns#tel");
        propertyMappings.put(Property.Id.TITLE,    "http://www.w3.org/2006/vcard/ns#title");
        propertyMappings.put(Property.Id.TZ,       "http://www.w3.org/2006/vcard/ns#tz");
        propertyMappings.put(Property.Id.UID,      "http://www.w3.org/2006/vcard/ns#uid");
        propertyMappings.put(Property.Id.URL,      "http://www.w3.org/2006/vcard/ns#url");

    }

    /**
     * Creates a new RDFParserBase that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create RDF model objects.
     */
    public VCardParser() {
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING,true);
    }

    /**
     * Creates a new RDFParserBase that will use the supplied ValueFactory to
     * create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    public VCardParser(ValueFactory valueFactory) {
        super(valueFactory);
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING,true);
    }


    /**
     * Gets the RDF format that this parser can parse.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return VCardFormat.FORMAT;
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
        try {
            for(VCard card : new VCardBuilder(in).buildAll()) {
                parseVCard(card);
            }
        } catch (ParserException e) {
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
        try {
            for(VCard card : new VCardBuilder(reader).buildAll()) {
                parseVCard(card);
            }
        } catch (ParserException e) {
            throw new RDFParseException(e);
        }
    }

    private void parseVCard(VCard vCard) throws RDFHandlerException, RDFParseException {
        URI uri;

        // create uri for resource representing the vcard based on the UID if present or on a random UUID
        if(vCard.getProperty(Property.Id.UID) != null) {
            uri = resolveURI(vCard.getProperty(Property.Id.UID).getValue());
        } else {
            uri = resolveURI(UUID.randomUUID().toString());
        }
        Resource t_vcard = createURI(NS_VCARD + "VCard");
        URI p_type       = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(uri,p_type,t_vcard));

        for(Property p : vCard.getProperties()) {
            if(propertyMappings.containsKey(p.getId())) {
                URI prop =  createURI(propertyMappings.get(p.getId()));

                switch (p.getId()) {
                    case ADR:
                        createAddress(uri,prop,(Address)p);
                        break;
                    case AGENT:
                        // distinguish text and uri agents
                        createAgent(uri,prop,(Agent)p);
                        break;
                    case CATEGORIES:
                        for(String category : p.getValue().split(",")) {
                            Literal literal = createLiteral(category,null,null);
                            rdfHandler.handleStatement(createStatement(uri, prop, literal));
                        }
                        break;
                    case EMAIL:
                        URI email = createURI("mailto:"+p.getValue());
                        rdfHandler.handleStatement(createStatement(uri,prop,email));
                        break;
                    case GEO:
                        createLocation(uri,prop,(Geo)p);
                        break;
                    case LOGO:
                        createLogo(uri,prop,(Logo)p);
                        break;
                    case N:
                        createName(uri,prop,(N)p);
                        break;
                    case ORG:
                        createOrganization(uri,prop,(Org)p);
                        break;
                    case PHOTO:
                        createPhoto(uri, prop, (Photo) p);
                        break;
                    case SOUND:
                        createSound(uri, prop, (Sound) p);
                        break;
                    case TEL:
                        createTel(uri, prop, (Telephone) p);
                        break;
                    case URL:
                        URI url = createURI(p.getValue());
                        rdfHandler.handleStatement(createStatement(uri,prop,url));
                        break;

                    default:
                        Literal literal = createLiteral(p.getValue(),null,null);
                        rdfHandler.handleStatement(createStatement(uri, prop, literal));
                }
            } else {
                log.warn("unknown property type {} with value {} not added to RDF representation",p.getId(), p.getValue());
            }
        }
    }


    private void createAddress(URI uri, URI prop, Address adr) throws RDFHandlerException, RDFParseException {
        Resource r_adr = createBNode();
        Resource t_adr = createURI(NS_VCARD + "Address");
        URI p_type     = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(r_adr,p_type,t_adr));

        if(adr.getCountry() != null) {
            URI p_country = createURI(NS_VCARD + "country-name");
            Literal v_country = createLiteral(adr.getCountry(),null,null);
            rdfHandler.handleStatement(createStatement(r_adr,p_country,v_country));
        }

        if(adr.getExtended() != null && !"".equals(adr.getExtended().trim())) {
            URI p_ext = createURI(NS_VCARD + "extended-address");
            Literal v_ext = createLiteral(adr.getExtended(),null,null);
            rdfHandler.handleStatement(createStatement(r_adr,p_ext,v_ext));
        }

        if(adr.getLocality() != null && !"".equals(adr.getLocality().trim())) {
            URI p_locality = createURI(NS_VCARD + "locality");
            Literal v_locality = createLiteral(adr.getLocality(),null,null);
            rdfHandler.handleStatement(createStatement(r_adr,p_locality,v_locality));
        }

        if(adr.getPoBox() != null && !"".equals(adr.getPoBox().trim())) {
            URI p_pobox = createURI(NS_VCARD + "post-office-box");
            Literal v_pobox = createLiteral(adr.getPoBox(),null,null);
            rdfHandler.handleStatement(createStatement(r_adr,p_pobox,v_pobox));
        }

        if(adr.getPostcode() != null  && !"".equals(adr.getPostcode().trim())) {
            URI p_postcode = createURI(NS_VCARD + "postal-code");
            Literal v_postcode = createLiteral(adr.getPostcode(),null,null);
            rdfHandler.handleStatement(createStatement(r_adr,p_postcode,v_postcode));
        }

        if(adr.getRegion() != null && !"".equals(adr.getRegion().trim())) {
            URI p_region = createURI(NS_VCARD + "region");
            Literal v_region = createLiteral(adr.getRegion(),null,null);
            rdfHandler.handleStatement(createStatement(r_adr,p_region,v_region));
        }

        if(adr.getStreet() != null) {
            URI p_street = createURI(NS_VCARD + "street-address");
            Literal v_street = createLiteral(adr.getStreet(),null,null);
            rdfHandler.handleStatement(createStatement(r_adr,p_street,v_street));
        }




        if(adr.getParameter(Parameter.Id.TYPE) != null) {
            Type type = (Type)adr.getParameter(Parameter.Id.TYPE);
            for(String value : type.getTypes()) {
                if("HOME".equals(value)) {
                    URI p_home = createURI(NS_VCARD + "homeAdr");
                    rdfHandler.handleStatement(createStatement(uri,p_home,r_adr));
                } else if("WORK".equals(value)) {
                    URI p_work = createURI(NS_VCARD + "workAdr");
                    rdfHandler.handleStatement(createStatement(uri,p_work,r_adr));
                } else {
                    rdfHandler.handleStatement(createStatement(uri,prop,r_adr));
                }
                break; // only first one
            }
        } else {
            rdfHandler.handleStatement(createStatement(uri,prop,r_adr));
        }
    }


    private void createAgent(URI uri, URI prop, Agent agent) throws RDFHandlerException, RDFParseException {
        if(agent.getUri() != null) {
            URI r_agent = createURI(agent.getUri().toString());
            rdfHandler.handleStatement(createStatement(uri,prop,r_agent));
        } else {
            log.warn("ignoring agent relation, since agent cannot be resolved");
        }
    }

    private void createLocation(URI uri, URI prop, Geo geo) throws RDFHandlerException, RDFParseException {
        Resource r_location = createBNode();
        Resource t_adr = createURI(NS_VCARD + "Location");
        URI p_type     = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(r_location,p_type,t_adr));

        URI p_latitute = createURI(NS_VCARD+"latitude");
        URI p_longitude = createURI(NS_VCARD+"longitude");
        URI t_decimal   = createURI("http://www.w3.org/2001/XMLSchema#double");

        Literal v_latitude = createLiteral(geo.getLatitude().toPlainString(),null,t_decimal);
        Literal v_longitude = createLiteral(geo.getLongitude().toPlainString(), null, t_decimal);

        rdfHandler.handleStatement(createStatement(r_location,p_latitute,v_latitude));
        rdfHandler.handleStatement(createStatement(r_location,p_longitude,v_longitude));
        rdfHandler.handleStatement(createStatement(uri,prop,r_location));
    }


    private void createLogo(URI uri, URI prop, Logo logo) throws RDFHandlerException, RDFParseException {
        if(logo.getUri() != null) {
            URI r_logo = createURI(logo.getUri().toString());
            rdfHandler.handleStatement(createStatement(uri,prop,r_logo));
        } else {
            log.warn("ignoring logo relation, since binary logos are not supported in RDF");
        }
    }


    private void createName(URI uri, URI prop, N name) throws RDFHandlerException, RDFParseException {
        Resource r_name = createBNode();
        Resource t_name = createURI(NS_VCARD + "Name");
        URI p_type      = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(r_name,p_type,t_name));

        if(name.getFamilyName() != null) {
            URI p_family_name = createURI(NS_VCARD + "family-name");
            Literal v_family_name = createLiteral(name.getFamilyName(),null,null);
            rdfHandler.handleStatement(createStatement(r_name,p_family_name,v_family_name));
        }

        if(name.getGivenName() != null) {
            URI p_given_name = createURI(NS_VCARD + "given-name");
            Literal v_given_name = createLiteral(name.getGivenName(),null,null);
            rdfHandler.handleStatement(createStatement(r_name,p_given_name,v_given_name));
        }

        if(name.getAdditionalNames() != null && name.getAdditionalNames().length > 0) {
            URI p_additional_name = createURI(NS_VCARD + "additional-name");
            for(String additionalName : name.getAdditionalNames()) {
                if(!"".equals(additionalName)) {
                    Literal v_additional_name = createLiteral(additionalName,null,null);
                    rdfHandler.handleStatement(createStatement(r_name,p_additional_name,v_additional_name));
                }
            }
        }

        if(name.getPrefixes() != null && name.getPrefixes().length > 0) {
            URI p_prefix = createURI(NS_VCARD + "honorific-prefix");
            for(String namePrefix : name.getPrefixes()) {
                if(!"".equals(namePrefix)) {
                    Literal v_prefix = createLiteral(namePrefix,null,null);
                    rdfHandler.handleStatement(createStatement(r_name,p_prefix,v_prefix));
                }
            }
        }

        if(name.getSuffixes() != null && name.getSuffixes().length > 0) {
            URI p_suffix = createURI(NS_VCARD + "honorific-suffix");
            for(String nameSuffix : name.getSuffixes()) {
                if(!"".equals(nameSuffix)) {
                    Literal v_suffix = createLiteral(nameSuffix,null,null);
                    rdfHandler.handleStatement(createStatement(r_name,p_suffix,v_suffix));
                }
            }
        }

        rdfHandler.handleStatement(createStatement(uri,prop,r_name));
    }


    private void createOrganization(URI uri, URI prop, Org org) throws RDFHandlerException, RDFParseException {
        for(String orgName : org.getValues()) {
            Resource r_name = createBNode();
            Resource t_org = createURI(NS_VCARD + "Organization");
            URI p_type     = createURI(NS_RDF + "type");
            rdfHandler.handleStatement(createStatement(r_name,p_type,t_org));

            URI p_name = createURI(NS_VCARD + "organization-name");
            Literal v_name = createLiteral(orgName,null,null);

            rdfHandler.handleStatement(createStatement(r_name,p_name,v_name));
            rdfHandler.handleStatement(createStatement(uri,prop,r_name));
        }
    }


    private void createPhoto(URI uri, URI prop, Photo photo) throws RDFHandlerException, RDFParseException {
        if(photo.getUri() != null) {
            URI r_logo = createURI(photo.getUri().toString());
            rdfHandler.handleStatement(createStatement(uri,prop,r_logo));
        } else {
            log.warn("ignoring photo relation, since binary photos are not supported in RDF");
        }
    }

    private void createSound(URI uri, URI prop, Sound sound) throws RDFHandlerException, RDFParseException {
        if(sound.getUri() != null) {
            URI r_logo = createURI(sound.getUri().toString());
            rdfHandler.handleStatement(createStatement(uri,prop,r_logo));
        } else {
            log.warn("ignoring photo relation, since binary photos are not supported in RDF");
        }
    }


    private void createTel(URI uri, URI prop, Telephone telephone) throws RDFHandlerException, RDFParseException {
        URI r_tel;

        if(telephone.getUri() != null) {
            r_tel = createURI(telephone.getUri().toString());
        } else {
            r_tel = createURI("tel:"+telephone.getValue());
        }

        if(telephone.getParameter(Parameter.Id.TYPE) != null) {
            Type type = (Type)telephone.getParameter(Parameter.Id.TYPE);
            for(String value : type.getTypes()) {
                if("HOME".equals(value)) {
                    URI p_home = createURI(NS_VCARD + "homeTel");
                    rdfHandler.handleStatement(createStatement(uri,p_home,r_tel));
                } else if("WORK".equals(value)) {
                    URI p_work = createURI(NS_VCARD + "workTel");
                    rdfHandler.handleStatement(createStatement(uri,p_work,r_tel));
                } else if("CELL".equals(value)) {
                    URI p_work = createURI(NS_VCARD + "mobileTel");
                    rdfHandler.handleStatement(createStatement(uri,p_work,r_tel));
                } else {
                    rdfHandler.handleStatement(createStatement(uri,prop,r_tel));
                }
                break; // only first one
            }

        } else {
            rdfHandler.handleStatement(createStatement(uri,prop,r_tel));
        }
    }

}
