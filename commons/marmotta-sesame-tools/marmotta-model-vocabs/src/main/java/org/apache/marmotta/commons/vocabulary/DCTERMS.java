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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Namespace DCTERMS
 */
public class DCTERMS {

    public static final String NAMESPACE = "http://purl.org/dc/terms/";

    public static final String PREFIX = "dcterms";

    /**
     * A resource that acts or has the power to act.
     */
    public static final IRI Agent;

    /**
     * A group of agents.
     */
    public static final IRI AgentClass;

    /**
     * A book, article, or other documentary resource.
     */
    public static final IRI BibliographicResource;

    /**
     * The set of regions in space defined by their geographic coordinates according to the DCMI Box Encoding Scheme.
     */
    public static final IRI Box;

    /**
     * The set of classes specified by the DCMI Type Vocabulary, used to categorize the nature or genre of the resource.
     */
    public static final IRI DCMIType;

    /**
     * The set of conceptual resources specified by the Dewey Decimal Classification.
     */
    public static final IRI DDC;

    /**
     * A digital resource format.
     */
    public static final IRI FileFormat;

    /**
     * A rate at which something recurs.
     */
    public static final IRI Frequency;

    /**
     * The set of media types specified by the Internet Assigned Numbers Authority.
     */
    public static final IRI IMT;

    /**
     * The set of codes listed in ISO 3166-1 for the representation of names of countries.
     */
    public static final IRI ISO3166;

    /**
     * The three-letter alphabetic codes listed in ISO639-2 for the representation of names of languages.
     */
    public static final IRI ISO639_2;

    /**
     * The set of three-letter codes listed in ISO 639-3 for the representation of names of languages.
     */
    public static final IRI ISO639_3;

    /**
     * The extent or range of judicial, law enforcement, or other authority.
     */
    public static final IRI Jurisdiction;

    /**
     * The set of conceptual resources specified by the Library of Congress Classification.
     */
    public static final IRI LCC;

    /**
     * The set of labeled concepts specified by the Library of Congress Subject Headings.
     */
    public static final IRI LCSH;

    /**
     * A legal document giving official permission to do something with a Resource.
     */
    public static final IRI LicenseDocument;

    /**
     * A system of signs, symbols, sounds, gestures, or rules used in communication.
     */
    public static final IRI LinguisticSystem;

    /**
     * A spatial region or named place.
     */
    public static final IRI Location;

    /**
     * A location, period of time, or jurisdiction.
     */
    public static final IRI LocationPeriodOrJurisdiction;

    /**
     * The set of labeled concepts specified by the Medical Subject Headings.
     */
    public static final IRI MESH;

    /**
     * A file format or physical medium.
     */
    public static final IRI MediaType;

    /**
     * A media type or extent.
     */
    public static final IRI MediaTypeOrExtent;

    /**
     * A method by which resources are added to a collection.
     */
    public static final IRI MethodOfAccrual;

    /**
     * A process that is used to engender knowledge, attitudes, and skills.
     */
    public static final IRI MethodOfInstruction;

    /**
     * The set of conceptual resources specified by the National Library of Medicine Classification.
     */
    public static final IRI NLM;

    /**
     * The set of time intervals defined by their limits according to the DCMI Period Encoding Scheme.
     */
    public static final IRI Period;

    /**
     * An interval of time that is named or defined by its start and end dates.
     */
    public static final IRI PeriodOfTime;

    /**
     * A physical material or carrier.
     */
    public static final IRI PhysicalMedium;

    /**
     * A material thing.
     */
    public static final IRI PhysicalResource;

    /**
     * The set of points in space defined by their geographic coordinates according to the DCMI Point Encoding Scheme.
     */
    public static final IRI Point;

    /**
     * A plan or course of action by an authority, intended to influence and determine decisions, actions, and other matters.
     */
    public static final IRI Policy;

    /**
     * A statement of any changes in ownership and custody of a resource since its creation that are significant for its authenticity, integrity, and interpretation.
     */
    public static final IRI ProvenanceStatement;

    /**
     * The set of tags, constructed according to RFC 1766, for the identification of languages.
     */
    public static final IRI RFC1766;

    /**
     * The set of tags constructed according to RFC 3066 for the identification of languages.
     */
    public static final IRI RFC3066;

    /**
     * The set of tags constructed according to RFC 4646 for the identification of languages.
     */
    public static final IRI RFC4646;

    /**
     * A statement about the intellectual property rights (IPR) held in or over a Resource, a legal document giving official permission to do something with a resource, or a statement about access rights.
     */
    public static final IRI RightsStatement;

    /**
     * A dimension or extent, or a time taken to play or execute.
     */
    public static final IRI SizeOrDuration;

    /**
     * A basis for comparison; a reference point against which other things can be evaluated.
     */
    public static final IRI Standard;

    /**
     * The set of places specified by the Getty Thesaurus of Geographic Names.
     */
    public static final IRI TGN;

    /**
     * The set of conceptual resources specified by the Universal Decimal Classification.
     */
    public static final IRI UDC;

    /**
     * The set of identifiers constructed according to the generic syntax for Uniform Resource Identifiers as specified by the Internet Engineering Task Force.
     */
    public static final IRI IRI;

    /**
     * The set of dates and times constructed according to the W3C Date and Time Formats Specification.
     */
    public static final IRI W3CDTF;

    /**
     * A summary of the resource.
     */
    public static final IRI abstract_;

    /**
     * Information about who can access the resource or an indication of its security status.
     */
    public static final IRI accessRights;

    /**
     * The method by which items are added to a collection.
     */
    public static final IRI accrualMethod;

    /**
     * The frequency with which items are added to a collection.
     */
    public static final IRI accrualPeriodicity;

    /**
     * The policy governing the addition of items to a collection.
     */
    public static final IRI accrualPolicy;

    /**
     * An alternative name for the resource.
     */
    public static final IRI alternative;

    /**
     * A class of entity for whom the resource is intended or useful.
     */
    public static final IRI audience;

    /**
     * Date (often a range) that the resource became or will become available.
     */
    public static final IRI available;

    /**
     * A bibliographic reference for the resource.
     */
    public static final IRI bibliographicCitation;

    /**
     * An established standard to which the described resource conforms.
     */
    public static final IRI conformsTo;

    /**
     * An entity responsible for making contributions to the resource.
     */
    public static final IRI contributor;

    /**
     * The spatial or temporal topic of the resource, the spatial applicability of the resource, or the jurisdiction under which the resource is relevant.
     */
    public static final IRI coverage;

    /**
     * Date of creation of the resource.
     */
    public static final IRI created;

    /**
     * An entity primarily responsible for making the resource.
     */
    public static final IRI creator;

    /**
     * A point or period of time associated with an event in the lifecycle of the resource.
     */
    public static final IRI date;

    /**
     * Date of acceptance of the resource.
     */
    public static final IRI dateAccepted;

    /**
     * Date of copyright.
     */
    public static final IRI dateCopyrighted;

    /**
     * Date of submission of the resource.
     */
    public static final IRI dateSubmitted;

    /**
     * An account of the resource.
     */
    public static final IRI description;

    /**
     * A class of entity, defined in terms of progression through an educational or training context, for which the described resource is intended.
     */
    public static final IRI educationLevel;

    /**
     * The size or duration of the resource.
     */
    public static final IRI extent;

    /**
     * The file format, physical medium, or dimensions of the resource.
     */
    public static final IRI format;

    /**
     * A related resource that is substantially the same as the pre-existing described resource, but in another format.
     */
    public static final IRI hasFormat;

    /**
     * A related resource that is included either physically or logically in the described resource.
     */
    public static final IRI hasPart;

    /**
     * A related resource that is a version, edition, or adaptation of the described resource.
     */
    public static final IRI hasVersion;

    /**
     * An unambiguous reference to the resource within a given context.
     */
    public static final IRI identifier;

    /**
     * A process, used to engender knowledge, attitudes and skills, that the described resource is designed to support.
     */
    public static final IRI instructionalMethod;

    /**
     * A related resource that is substantially the same as the described resource, but in another format.
     */
    public static final IRI isFormatOf;

    /**
     * A related resource in which the described resource is physically or logically included.
     */
    public static final IRI isPartOf;

    /**
     * A related resource that references, cites, or otherwise points to the described resource.
     */
    public static final IRI isReferencedBy;

    /**
     * A related resource that supplants, displaces, or supersedes the described resource.
     */
    public static final IRI isReplacedBy;

    /**
     * A related resource that requires the described resource to support its function, delivery, or coherence.
     */
    public static final IRI isRequiredBy;

    /**
     * A related resource of which the described resource is a version, edition, or adaptation.
     */
    public static final IRI isVersionOf;

    /**
     * Date of formal issuance (e.g., publication) of the resource.
     */
    public static final IRI issued;

    /**
     * A language of the resource.
     */
    public static final IRI language;

    /**
     * A legal document giving official permission to do something with the resource.
     */
    public static final IRI license;

    /**
     * An entity that mediates access to the resource and for whom the resource is intended or useful.
     */
    public static final IRI mediator;

    /**
     * The material or physical carrier of the resource.
     */
    public static final IRI medium;

    /**
     * Date on which the resource was changed.
     */
    public static final IRI modified;

    /**
     * A statement of any changes in ownership and custody of the resource since its creation that are significant for its authenticity, integrity, and interpretation.
     */
    public static final IRI provenance;

    /**
     * An entity responsible for making the resource available.
     */
    public static final IRI publisher;

    /**
     * A related resource that is referenced, cited, or otherwise pointed to by the described resource.
     */
    public static final IRI references;

    /**
     * A related resource.
     */
    public static final IRI relation;

    /**
     * A related resource that is supplanted, displaced, or superseded by the described resource.
     */
    public static final IRI replaces;

    /**
     * A related resource that is required by the described resource to support its function, delivery, or coherence.
     */
    public static final IRI requires;

    /**
     * Information about rights held in and over the resource.
     */
    public static final IRI rights;

    /**
     * A person or organization owning or managing rights over the resource.
     */
    public static final IRI rightsHolder;

    /**
     * A related resource from which the described resource is derived.
     */
    public static final IRI source;

    /**
     * Spatial characteristics of the resource.
     */
    public static final IRI spatial;

    /**
     * The topic of the resource.
     */
    public static final IRI subject;

    /**
     * A list of subunits of the resource.
     */
    public static final IRI tableOfContents;

    /**
     * Temporal characteristics of the resource.
     */
    public static final IRI temporal;

    /**
     * A name given to the resource
     */
    public static final IRI title;

    /**
     * The nature or genre of the resource.
     */
    public static final IRI type;

    /**
     * Date (often a range) of validity of a resource.
     */
    public static final IRI valid;


    static{
        ValueFactory factory = SimpleValueFactory.getInstance();
        Agent = factory.createIRI(DCTERMS.NAMESPACE, "Agent");
        AgentClass = factory.createIRI(DCTERMS.NAMESPACE, "AgentClass");
        BibliographicResource = factory.createIRI(DCTERMS.NAMESPACE, "BibliographicResource");
        Box = factory.createIRI(DCTERMS.NAMESPACE, "Box");
        DCMIType = factory.createIRI(DCTERMS.NAMESPACE, "DCMIType");
        DDC = factory.createIRI(DCTERMS.NAMESPACE, "DDC");
        FileFormat = factory.createIRI(DCTERMS.NAMESPACE, "FileFormat");
        Frequency = factory.createIRI(DCTERMS.NAMESPACE, "Frequency");
        IMT = factory.createIRI(DCTERMS.NAMESPACE, "IMT");
        ISO3166 = factory.createIRI(DCTERMS.NAMESPACE, "ISO3166");
        ISO639_2 = factory.createIRI(DCTERMS.NAMESPACE, "ISO639_2");
        ISO639_3 = factory.createIRI(DCTERMS.NAMESPACE, "ISO639_3");
        Jurisdiction = factory.createIRI(DCTERMS.NAMESPACE, "Jurisdiction");
        LCC = factory.createIRI(DCTERMS.NAMESPACE, "LCC");
        LCSH = factory.createIRI(DCTERMS.NAMESPACE, "LCSH");
        LicenseDocument = factory.createIRI(DCTERMS.NAMESPACE, "LicenseDocument");
        LinguisticSystem = factory.createIRI(DCTERMS.NAMESPACE, "LinguisticSystem");
        Location = factory.createIRI(DCTERMS.NAMESPACE, "Location");
        LocationPeriodOrJurisdiction = factory.createIRI(DCTERMS.NAMESPACE, "LocationPeriodOrJurisdiction");
        MESH = factory.createIRI(DCTERMS.NAMESPACE, "MESH");
        MediaType = factory.createIRI(DCTERMS.NAMESPACE, "MediaType");
        MediaTypeOrExtent = factory.createIRI(DCTERMS.NAMESPACE, "MediaTypeOrExtent");
        MethodOfAccrual = factory.createIRI(DCTERMS.NAMESPACE, "MethodOfAccrual");
        MethodOfInstruction = factory.createIRI(DCTERMS.NAMESPACE, "MethodOfInstruction");
        NLM = factory.createIRI(DCTERMS.NAMESPACE, "NLM");
        Period = factory.createIRI(DCTERMS.NAMESPACE, "Period");
        PeriodOfTime = factory.createIRI(DCTERMS.NAMESPACE, "PeriodOfTime");
        PhysicalMedium = factory.createIRI(DCTERMS.NAMESPACE, "PhysicalMedium");
        PhysicalResource = factory.createIRI(DCTERMS.NAMESPACE, "PhysicalResource");
        Point = factory.createIRI(DCTERMS.NAMESPACE, "Point");
        Policy = factory.createIRI(DCTERMS.NAMESPACE, "Policy");
        ProvenanceStatement = factory.createIRI(DCTERMS.NAMESPACE, "ProvenanceStatement");
        RFC1766 = factory.createIRI(DCTERMS.NAMESPACE, "RFC1766");
        RFC3066 = factory.createIRI(DCTERMS.NAMESPACE, "RFC3066");
        RFC4646 = factory.createIRI(DCTERMS.NAMESPACE, "RFC4646");
        RightsStatement = factory.createIRI(DCTERMS.NAMESPACE, "RightsStatement");
        SizeOrDuration = factory.createIRI(DCTERMS.NAMESPACE, "SizeOrDuration");
        Standard = factory.createIRI(DCTERMS.NAMESPACE, "Standard");
        TGN = factory.createIRI(DCTERMS.NAMESPACE, "TGN");
        UDC = factory.createIRI(DCTERMS.NAMESPACE, "UDC");
        IRI = factory.createIRI(DCTERMS.NAMESPACE, "IRI");
        W3CDTF = factory.createIRI(DCTERMS.NAMESPACE, "W3CDTF");
        abstract_ = factory.createIRI(DCTERMS.NAMESPACE, "abstract");
        accessRights = factory.createIRI(DCTERMS.NAMESPACE, "accessRights");
        accrualMethod = factory.createIRI(DCTERMS.NAMESPACE, "accrualMethod");
        accrualPeriodicity = factory.createIRI(DCTERMS.NAMESPACE, "accrualPeriodicity");
        accrualPolicy = factory.createIRI(DCTERMS.NAMESPACE, "accrualPolicy");
        alternative = factory.createIRI(DCTERMS.NAMESPACE, "alternative");
        audience = factory.createIRI(DCTERMS.NAMESPACE, "audience");
        available = factory.createIRI(DCTERMS.NAMESPACE, "available");
        bibliographicCitation = factory.createIRI(DCTERMS.NAMESPACE, "bibliographicCitation");
        conformsTo = factory.createIRI(DCTERMS.NAMESPACE, "conformsTo");
        contributor = factory.createIRI(DCTERMS.NAMESPACE, "contributor");
        coverage = factory.createIRI(DCTERMS.NAMESPACE, "coverage");
        created = factory.createIRI(DCTERMS.NAMESPACE, "created");
        creator = factory.createIRI(DCTERMS.NAMESPACE, "creator");
        date = factory.createIRI(DCTERMS.NAMESPACE, "date");
        dateAccepted = factory.createIRI(DCTERMS.NAMESPACE, "dateAccepted");
        dateCopyrighted = factory.createIRI(DCTERMS.NAMESPACE, "dateCopyrighted");
        dateSubmitted = factory.createIRI(DCTERMS.NAMESPACE, "dateSubmitted");
        description = factory.createIRI(DCTERMS.NAMESPACE, "description");
        educationLevel = factory.createIRI(DCTERMS.NAMESPACE, "educationLevel");
        extent = factory.createIRI(DCTERMS.NAMESPACE, "extent");
        format = factory.createIRI(DCTERMS.NAMESPACE, "format");
        hasFormat = factory.createIRI(DCTERMS.NAMESPACE, "hasFormat");
        hasPart = factory.createIRI(DCTERMS.NAMESPACE, "hasPart");
        hasVersion = factory.createIRI(DCTERMS.NAMESPACE, "hasVersion");
        identifier = factory.createIRI(DCTERMS.NAMESPACE, "identifier");
        instructionalMethod = factory.createIRI(DCTERMS.NAMESPACE, "instructionalMethod");
        isFormatOf = factory.createIRI(DCTERMS.NAMESPACE, "isFormatOf");
        isPartOf = factory.createIRI(DCTERMS.NAMESPACE, "isPartOf");
        isReferencedBy = factory.createIRI(DCTERMS.NAMESPACE, "isReferencedBy");
        isReplacedBy = factory.createIRI(DCTERMS.NAMESPACE, "isReplacedBy");
        isRequiredBy = factory.createIRI(DCTERMS.NAMESPACE, "isRequiredBy");
        isVersionOf = factory.createIRI(DCTERMS.NAMESPACE, "isVersionOf");
        issued = factory.createIRI(DCTERMS.NAMESPACE, "issued");
        language = factory.createIRI(DCTERMS.NAMESPACE, "language");
        license = factory.createIRI(DCTERMS.NAMESPACE, "license");
        mediator = factory.createIRI(DCTERMS.NAMESPACE, "mediator");
        medium = factory.createIRI(DCTERMS.NAMESPACE, "medium");
        modified = factory.createIRI(DCTERMS.NAMESPACE, "modified");
        provenance = factory.createIRI(DCTERMS.NAMESPACE, "provenance");
        publisher = factory.createIRI(DCTERMS.NAMESPACE, "publisher");
        references = factory.createIRI(DCTERMS.NAMESPACE, "references");
        relation = factory.createIRI(DCTERMS.NAMESPACE, "relation");
        replaces = factory.createIRI(DCTERMS.NAMESPACE, "replaces");
        requires = factory.createIRI(DCTERMS.NAMESPACE, "requires");
        rights = factory.createIRI(DCTERMS.NAMESPACE, "rights");
        rightsHolder = factory.createIRI(DCTERMS.NAMESPACE, "rightsHolder");
        source = factory.createIRI(DCTERMS.NAMESPACE, "source");
        spatial = factory.createIRI(DCTERMS.NAMESPACE, "spatial");
        subject = factory.createIRI(DCTERMS.NAMESPACE, "subject");
        tableOfContents = factory.createIRI(DCTERMS.NAMESPACE, "tableOfContents");
        temporal = factory.createIRI(DCTERMS.NAMESPACE, "temporal");
        title = factory.createIRI(DCTERMS.NAMESPACE, "title");
        type = factory.createIRI(DCTERMS.NAMESPACE, "type");
        valid = factory.createIRI(DCTERMS.NAMESPACE, "valid");
    }
}
