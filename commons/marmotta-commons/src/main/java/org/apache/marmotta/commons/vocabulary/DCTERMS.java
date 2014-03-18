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
package org.apache.marmotta.commons.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Namespace DCTERMS
 */
public class DCTERMS {

    public static final String NAMESPACE = "http://purl.org/dc/terms/";

    public static final String PREFIX = "dcterms";

    /**
     * A resource that acts or has the power to act.
     */
    public static final URI Agent;

    /**
     * A group of agents.
     */
    public static final URI AgentClass;

    /**
     * A book, article, or other documentary resource.
     */
    public static final URI BibliographicResource;

    /**
     * The set of regions in space defined by their geographic coordinates according to the DCMI Box Encoding Scheme.
     */
    public static final URI Box;

    /**
     * The set of classes specified by the DCMI Type Vocabulary, used to categorize the nature or genre of the resource.
     */
    public static final URI DCMIType;

    /**
     * The set of conceptual resources specified by the Dewey Decimal Classification.
     */
    public static final URI DDC;

    /**
     * A digital resource format.
     */
    public static final URI FileFormat;

    /**
     * A rate at which something recurs.
     */
    public static final URI Frequency;

    /**
     * The set of media types specified by the Internet Assigned Numbers Authority.
     */
    public static final URI IMT;

    /**
     * The set of codes listed in ISO 3166-1 for the representation of names of countries.
     */
    public static final URI ISO3166;

    /**
     * The three-letter alphabetic codes listed in ISO639-2 for the representation of names of languages.
     */
    public static final URI ISO639_2;

    /**
     * The set of three-letter codes listed in ISO 639-3 for the representation of names of languages.
     */
    public static final URI ISO639_3;

    /**
     * The extent or range of judicial, law enforcement, or other authority.
     */
    public static final URI Jurisdiction;

    /**
     * The set of conceptual resources specified by the Library of Congress Classification.
     */
    public static final URI LCC;

    /**
     * The set of labeled concepts specified by the Library of Congress Subject Headings.
     */
    public static final URI LCSH;

    /**
     * A legal document giving official permission to do something with a Resource.
     */
    public static final URI LicenseDocument;

    /**
     * A system of signs, symbols, sounds, gestures, or rules used in communication.
     */
    public static final URI LinguisticSystem;

    /**
     * A spatial region or named place.
     */
    public static final URI Location;

    /**
     * A location, period of time, or jurisdiction.
     */
    public static final URI LocationPeriodOrJurisdiction;

    /**
     * The set of labeled concepts specified by the Medical Subject Headings.
     */
    public static final URI MESH;

    /**
     * A file format or physical medium.
     */
    public static final URI MediaType;

    /**
     * A media type or extent.
     */
    public static final URI MediaTypeOrExtent;

    /**
     * A method by which resources are added to a collection.
     */
    public static final URI MethodOfAccrual;

    /**
     * A process that is used to engender knowledge, attitudes, and skills.
     */
    public static final URI MethodOfInstruction;

    /**
     * The set of conceptual resources specified by the National Library of Medicine Classification.
     */
    public static final URI NLM;

    /**
     * The set of time intervals defined by their limits according to the DCMI Period Encoding Scheme.
     */
    public static final URI Period;

    /**
     * An interval of time that is named or defined by its start and end dates.
     */
    public static final URI PeriodOfTime;

    /**
     * A physical material or carrier.
     */
    public static final URI PhysicalMedium;

    /**
     * A material thing.
     */
    public static final URI PhysicalResource;

    /**
     * The set of points in space defined by their geographic coordinates according to the DCMI Point Encoding Scheme.
     */
    public static final URI Point;

    /**
     * A plan or course of action by an authority, intended to influence and determine decisions, actions, and other matters.
     */
    public static final URI Policy;

    /**
     * A statement of any changes in ownership and custody of a resource since its creation that are significant for its authenticity, integrity, and interpretation.
     */
    public static final URI ProvenanceStatement;

    /**
     * The set of tags, constructed according to RFC 1766, for the identification of languages.
     */
    public static final URI RFC1766;

    /**
     * The set of tags constructed according to RFC 3066 for the identification of languages.
     */
    public static final URI RFC3066;

    /**
     * The set of tags constructed according to RFC 4646 for the identification of languages.
     */
    public static final URI RFC4646;

    /**
     * A statement about the intellectual property rights (IPR) held in or over a Resource, a legal document giving official permission to do something with a resource, or a statement about access rights.
     */
    public static final URI RightsStatement;

    /**
     * A dimension or extent, or a time taken to play or execute.
     */
    public static final URI SizeOrDuration;

    /**
     * A basis for comparison; a reference point against which other things can be evaluated.
     */
    public static final URI Standard;

    /**
     * The set of places specified by the Getty Thesaurus of Geographic Names.
     */
    public static final URI TGN;

    /**
     * The set of conceptual resources specified by the Universal Decimal Classification.
     */
    public static final URI UDC;

    /**
     * The set of identifiers constructed according to the generic syntax for Uniform Resource Identifiers as specified by the Internet Engineering Task Force.
     */
    public static final URI URI;

    /**
     * The set of dates and times constructed according to the W3C Date and Time Formats Specification.
     */
    public static final URI W3CDTF;

    /**
     * A summary of the resource.
     */
    public static final URI abstract_;

    /**
     * Information about who can access the resource or an indication of its security status.
     */
    public static final URI accessRights;

    /**
     * The method by which items are added to a collection.
     */
    public static final URI accrualMethod;

    /**
     * The frequency with which items are added to a collection.
     */
    public static final URI accrualPeriodicity;

    /**
     * The policy governing the addition of items to a collection.
     */
    public static final URI accrualPolicy;

    /**
     * An alternative name for the resource.
     */
    public static final URI alternative;

    /**
     * A class of entity for whom the resource is intended or useful.
     */
    public static final URI audience;

    /**
     * Date (often a range) that the resource became or will become available.
     */
    public static final URI available;

    /**
     * A bibliographic reference for the resource.
     */
    public static final URI bibliographicCitation;

    /**
     * An established standard to which the described resource conforms.
     */
    public static final URI conformsTo;

    /**
     * An entity responsible for making contributions to the resource.
     */
    public static final URI contributor;

    /**
     * The spatial or temporal topic of the resource, the spatial applicability of the resource, or the jurisdiction under which the resource is relevant.
     */
    public static final URI coverage;

    /**
     * Date of creation of the resource.
     */
    public static final URI created;

    /**
     * An entity primarily responsible for making the resource.
     */
    public static final URI creator;

    /**
     * A point or period of time associated with an event in the lifecycle of the resource.
     */
    public static final URI date;

    /**
     * Date of acceptance of the resource.
     */
    public static final URI dateAccepted;

    /**
     * Date of copyright.
     */
    public static final URI dateCopyrighted;

    /**
     * Date of submission of the resource.
     */
    public static final URI dateSubmitted;

    /**
     * An account of the resource.
     */
    public static final URI description;

    /**
     * A class of entity, defined in terms of progression through an educational or training context, for which the described resource is intended.
     */
    public static final URI educationLevel;

    /**
     * The size or duration of the resource.
     */
    public static final URI extent;

    /**
     * The file format, physical medium, or dimensions of the resource.
     */
    public static final URI format;

    /**
     * A related resource that is substantially the same as the pre-existing described resource, but in another format.
     */
    public static final URI hasFormat;

    /**
     * A related resource that is included either physically or logically in the described resource.
     */
    public static final URI hasPart;

    /**
     * A related resource that is a version, edition, or adaptation of the described resource.
     */
    public static final URI hasVersion;

    /**
     * An unambiguous reference to the resource within a given context.
     */
    public static final URI identifier;

    /**
     * A process, used to engender knowledge, attitudes and skills, that the described resource is designed to support.
     */
    public static final URI instructionalMethod;

    /**
     * A related resource that is substantially the same as the described resource, but in another format.
     */
    public static final URI isFormatOf;

    /**
     * A related resource in which the described resource is physically or logically included.
     */
    public static final URI isPartOf;

    /**
     * A related resource that references, cites, or otherwise points to the described resource.
     */
    public static final URI isReferencedBy;

    /**
     * A related resource that supplants, displaces, or supersedes the described resource.
     */
    public static final URI isReplacedBy;

    /**
     * A related resource that requires the described resource to support its function, delivery, or coherence.
     */
    public static final URI isRequiredBy;

    /**
     * A related resource of which the described resource is a version, edition, or adaptation.
     */
    public static final URI isVersionOf;

    /**
     * Date of formal issuance (e.g., publication) of the resource.
     */
    public static final URI issued;

    /**
     * A language of the resource.
     */
    public static final URI language;

    /**
     * A legal document giving official permission to do something with the resource.
     */
    public static final URI license;

    /**
     * An entity that mediates access to the resource and for whom the resource is intended or useful.
     */
    public static final URI mediator;

    /**
     * The material or physical carrier of the resource.
     */
    public static final URI medium;

    /**
     * Date on which the resource was changed.
     */
    public static final URI modified;

    /**
     * A statement of any changes in ownership and custody of the resource since its creation that are significant for its authenticity, integrity, and interpretation.
     */
    public static final URI provenance;

    /**
     * An entity responsible for making the resource available.
     */
    public static final URI publisher;

    /**
     * A related resource that is referenced, cited, or otherwise pointed to by the described resource.
     */
    public static final URI references;

    /**
     * A related resource.
     */
    public static final URI relation;

    /**
     * A related resource that is supplanted, displaced, or superseded by the described resource.
     */
    public static final URI replaces;

    /**
     * A related resource that is required by the described resource to support its function, delivery, or coherence.
     */
    public static final URI requires;

    /**
     * Information about rights held in and over the resource.
     */
    public static final URI rights;

    /**
     * A person or organization owning or managing rights over the resource.
     */
    public static final URI rightsHolder;

    /**
     * A related resource from which the described resource is derived.
     */
    public static final URI source;

    /**
     * Spatial characteristics of the resource.
     */
    public static final URI spatial;

    /**
     * The topic of the resource.
     */
    public static final URI subject;

    /**
     * A list of subunits of the resource.
     */
    public static final URI tableOfContents;

    /**
     * Temporal characteristics of the resource.
     */
    public static final URI temporal;

    /**
     * A name given to the resource
     */
    public static final URI title;

    /**
     * The nature or genre of the resource.
     */
    public static final URI type;

    /**
     * Date (often a range) of validity of a resource.
     */
    public static final URI valid;


    static{
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Agent = factory.createURI(DCTERMS.NAMESPACE, "Agent");
        AgentClass = factory.createURI(DCTERMS.NAMESPACE, "AgentClass");
        BibliographicResource = factory.createURI(DCTERMS.NAMESPACE, "BibliographicResource");
        Box = factory.createURI(DCTERMS.NAMESPACE, "Box");
        DCMIType = factory.createURI(DCTERMS.NAMESPACE, "DCMIType");
        DDC = factory.createURI(DCTERMS.NAMESPACE, "DDC");
        FileFormat = factory.createURI(DCTERMS.NAMESPACE, "FileFormat");
        Frequency = factory.createURI(DCTERMS.NAMESPACE, "Frequency");
        IMT = factory.createURI(DCTERMS.NAMESPACE, "IMT");
        ISO3166 = factory.createURI(DCTERMS.NAMESPACE, "ISO3166");
        ISO639_2 = factory.createURI(DCTERMS.NAMESPACE, "ISO639_2");
        ISO639_3 = factory.createURI(DCTERMS.NAMESPACE, "ISO639_3");
        Jurisdiction = factory.createURI(DCTERMS.NAMESPACE, "Jurisdiction");
        LCC = factory.createURI(DCTERMS.NAMESPACE, "LCC");
        LCSH = factory.createURI(DCTERMS.NAMESPACE, "LCSH");
        LicenseDocument = factory.createURI(DCTERMS.NAMESPACE, "LicenseDocument");
        LinguisticSystem = factory.createURI(DCTERMS.NAMESPACE, "LinguisticSystem");
        Location = factory.createURI(DCTERMS.NAMESPACE, "Location");
        LocationPeriodOrJurisdiction = factory.createURI(DCTERMS.NAMESPACE, "LocationPeriodOrJurisdiction");
        MESH = factory.createURI(DCTERMS.NAMESPACE, "MESH");
        MediaType = factory.createURI(DCTERMS.NAMESPACE, "MediaType");
        MediaTypeOrExtent = factory.createURI(DCTERMS.NAMESPACE, "MediaTypeOrExtent");
        MethodOfAccrual = factory.createURI(DCTERMS.NAMESPACE, "MethodOfAccrual");
        MethodOfInstruction = factory.createURI(DCTERMS.NAMESPACE, "MethodOfInstruction");
        NLM = factory.createURI(DCTERMS.NAMESPACE, "NLM");
        Period = factory.createURI(DCTERMS.NAMESPACE, "Period");
        PeriodOfTime = factory.createURI(DCTERMS.NAMESPACE, "PeriodOfTime");
        PhysicalMedium = factory.createURI(DCTERMS.NAMESPACE, "PhysicalMedium");
        PhysicalResource = factory.createURI(DCTERMS.NAMESPACE, "PhysicalResource");
        Point = factory.createURI(DCTERMS.NAMESPACE, "Point");
        Policy = factory.createURI(DCTERMS.NAMESPACE, "Policy");
        ProvenanceStatement = factory.createURI(DCTERMS.NAMESPACE, "ProvenanceStatement");
        RFC1766 = factory.createURI(DCTERMS.NAMESPACE, "RFC1766");
        RFC3066 = factory.createURI(DCTERMS.NAMESPACE, "RFC3066");
        RFC4646 = factory.createURI(DCTERMS.NAMESPACE, "RFC4646");
        RightsStatement = factory.createURI(DCTERMS.NAMESPACE, "RightsStatement");
        SizeOrDuration = factory.createURI(DCTERMS.NAMESPACE, "SizeOrDuration");
        Standard = factory.createURI(DCTERMS.NAMESPACE, "Standard");
        TGN = factory.createURI(DCTERMS.NAMESPACE, "TGN");
        UDC = factory.createURI(DCTERMS.NAMESPACE, "UDC");
        URI = factory.createURI(DCTERMS.NAMESPACE, "URI");
        W3CDTF = factory.createURI(DCTERMS.NAMESPACE, "W3CDTF");
        abstract_ = factory.createURI(DCTERMS.NAMESPACE, "abstract");
        accessRights = factory.createURI(DCTERMS.NAMESPACE, "accessRights");
        accrualMethod = factory.createURI(DCTERMS.NAMESPACE, "accrualMethod");
        accrualPeriodicity = factory.createURI(DCTERMS.NAMESPACE, "accrualPeriodicity");
        accrualPolicy = factory.createURI(DCTERMS.NAMESPACE, "accrualPolicy");
        alternative = factory.createURI(DCTERMS.NAMESPACE, "alternative");
        audience = factory.createURI(DCTERMS.NAMESPACE, "audience");
        available = factory.createURI(DCTERMS.NAMESPACE, "available");
        bibliographicCitation = factory.createURI(DCTERMS.NAMESPACE, "bibliographicCitation");
        conformsTo = factory.createURI(DCTERMS.NAMESPACE, "conformsTo");
        contributor = factory.createURI(DCTERMS.NAMESPACE, "contributor");
        coverage = factory.createURI(DCTERMS.NAMESPACE, "coverage");
        created = factory.createURI(DCTERMS.NAMESPACE, "created");
        creator = factory.createURI(DCTERMS.NAMESPACE, "creator");
        date = factory.createURI(DCTERMS.NAMESPACE, "date");
        dateAccepted = factory.createURI(DCTERMS.NAMESPACE, "dateAccepted");
        dateCopyrighted = factory.createURI(DCTERMS.NAMESPACE, "dateCopyrighted");
        dateSubmitted = factory.createURI(DCTERMS.NAMESPACE, "dateSubmitted");
        description = factory.createURI(DCTERMS.NAMESPACE, "description");
        educationLevel = factory.createURI(DCTERMS.NAMESPACE, "educationLevel");
        extent = factory.createURI(DCTERMS.NAMESPACE, "extent");
        format = factory.createURI(DCTERMS.NAMESPACE, "format");
        hasFormat = factory.createURI(DCTERMS.NAMESPACE, "hasFormat");
        hasPart = factory.createURI(DCTERMS.NAMESPACE, "hasPart");
        hasVersion = factory.createURI(DCTERMS.NAMESPACE, "hasVersion");
        identifier = factory.createURI(DCTERMS.NAMESPACE, "identifier");
        instructionalMethod = factory.createURI(DCTERMS.NAMESPACE, "instructionalMethod");
        isFormatOf = factory.createURI(DCTERMS.NAMESPACE, "isFormatOf");
        isPartOf = factory.createURI(DCTERMS.NAMESPACE, "isPartOf");
        isReferencedBy = factory.createURI(DCTERMS.NAMESPACE, "isReferencedBy");
        isReplacedBy = factory.createURI(DCTERMS.NAMESPACE, "isReplacedBy");
        isRequiredBy = factory.createURI(DCTERMS.NAMESPACE, "isRequiredBy");
        isVersionOf = factory.createURI(DCTERMS.NAMESPACE, "isVersionOf");
        issued = factory.createURI(DCTERMS.NAMESPACE, "issued");
        language = factory.createURI(DCTERMS.NAMESPACE, "language");
        license = factory.createURI(DCTERMS.NAMESPACE, "license");
        mediator = factory.createURI(DCTERMS.NAMESPACE, "mediator");
        medium = factory.createURI(DCTERMS.NAMESPACE, "medium");
        modified = factory.createURI(DCTERMS.NAMESPACE, "modified");
        provenance = factory.createURI(DCTERMS.NAMESPACE, "provenance");
        publisher = factory.createURI(DCTERMS.NAMESPACE, "publisher");
        references = factory.createURI(DCTERMS.NAMESPACE, "references");
        relation = factory.createURI(DCTERMS.NAMESPACE, "relation");
        replaces = factory.createURI(DCTERMS.NAMESPACE, "replaces");
        requires = factory.createURI(DCTERMS.NAMESPACE, "requires");
        rights = factory.createURI(DCTERMS.NAMESPACE, "rights");
        rightsHolder = factory.createURI(DCTERMS.NAMESPACE, "rightsHolder");
        source = factory.createURI(DCTERMS.NAMESPACE, "source");
        spatial = factory.createURI(DCTERMS.NAMESPACE, "spatial");
        subject = factory.createURI(DCTERMS.NAMESPACE, "subject");
        tableOfContents = factory.createURI(DCTERMS.NAMESPACE, "tableOfContents");
        temporal = factory.createURI(DCTERMS.NAMESPACE, "temporal");
        title = factory.createURI(DCTERMS.NAMESPACE, "title");
        type = factory.createURI(DCTERMS.NAMESPACE, "type");
        valid = factory.createURI(DCTERMS.NAMESPACE, "valid");
    }
}
