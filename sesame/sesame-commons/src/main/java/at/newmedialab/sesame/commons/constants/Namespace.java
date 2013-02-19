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
package at.newmedialab.sesame.commons.constants;

/**
 * Created with IntelliJ IDEA.
 * User: tkurz
 * Date: 10.11.12
 * Time: 12:12
 * To change this template use File | Settings | File Templates.
 */
public class Namespace {

    /**
     * Namespace SKOS
     */
    public static class SKOS {
        /**
         * A meaningful collection of concepts.
         */
        public static final String Collection = "http://www.w3.org/2004/02/skos/core#Collection";
        /**
         * An idea or notion; a unit of thought.
         */
        public static final String Concept = "http://www.w3.org/2004/02/skos/core#Concept";
        /**
         * A set of concepts, optionally including statements about semantic relationships between those concepts.
         */
        public static final String ConceptScheme = "http://www.w3.org/2004/02/skos/core#ConceptScheme";
        /**
         * An ordered collection of concepts, where both the grouping and the ordering are meaningful.
         */
        public static final String OrderedCollection = "http://www.w3.org/2004/02/skos/core#OrderedCollection";
        /**
         * An alternative lexical label for a resource.
         */
        public static final String altLabel = "http://www.w3.org/2004/02/skos/core#altLabel";
        /**
         * skos:broadMatch is used to state a hierarchical mapping link between two conceptual resources in different concept schemes.
         */
        public static final String broadMatch = "http://www.w3.org/2004/02/skos/core#broadMatch";
        /**
         * Relates a concept to a concept that is more general in meaning.
         */
        public static final String broader = "http://www.w3.org/2004/02/skos/core#broader";
        /**
         * skos:broaderTransitive is a transitive superproperty of skos:broader.
         */
        public static final String broaderTransitive = "http://www.w3.org/2004/02/skos/core#broaderTransitive";
        /**
         * A note about a modification to a concept.
         */
        public static final String changeNote = "http://www.w3.org/2004/02/skos/core#changeNote";
        /**
         * skos:closeMatch is used to link two concepts that are sufficiently similar that they can be used interchangeably in some information retrieval applications. In order to avoid the possibility of "compound errors" when combining mappings across more than two concept schemes, skos:closeMatch is not declared to be a transitive property.
         */
        public static final String closeMatch = "http://www.w3.org/2004/02/skos/core#closeMatch";
        /**
         * A statement or formal explanation of the meaning of a concept.
         */
        public static final String definition = "http://www.w3.org/2004/02/skos/core#definition";
        /**
         * A note for an editor, translator or maintainer of the vocabulary.
         */
        public static final String editorialNote = "http://www.w3.org/2004/02/skos/core#editorialNote";
        /**
         * skos:exactMatch is used to link two concepts, indicating a high degree of confidence that the concepts can be used interchangeably across a wide range of information retrieval applications. skos:exactMatch is a transitive property, and is a sub-property of skos:closeMatch.
         */
        public static final String exactMatch = "http://www.w3.org/2004/02/skos/core#exactMatch";
        /**
         * An example of the use of a concept.
         */
        public static final String example = "http://www.w3.org/2004/02/skos/core#example";
        /**
         * Relates, by convention, a concept scheme to a concept which is topmost in the broader/narrower concept hierarchies for that scheme, providing an entry point to these hierarchies.
         */
        public static final String hasTopConcept = "http://www.w3.org/2004/02/skos/core#hasTopConcept";
        /**
         * A lexical label for a resource that should be hidden when generating visual displays of the resource, but should still be accessible to free text search operations.
         */
        public static final String hiddenLabel = "http://www.w3.org/2004/02/skos/core#hiddenLabel";
        /**
         * A note about the past state/use/meaning of a concept.
         */
        public static final String historyNote = "http://www.w3.org/2004/02/skos/core#historyNote";
        /**
         * Relates a resource (for example a concept) to a concept scheme in which it is included.
         */
        public static final String inScheme = "http://www.w3.org/2004/02/skos/core#inScheme";
        /**
         * Relates two concepts coming, by convention, from different schemes, and that have comparable meanings
         */
        public static final String mappingRelation = "http://www.w3.org/2004/02/skos/core#mappingRelation";
        /**
         * Relates a collection to one of its members.
         */
        public static final String member = "http://www.w3.org/2004/02/skos/core#member";
        /**
         * Relates an ordered collection to the RDF list containing its members.
         */
        public static final String memberList = "http://www.w3.org/2004/02/skos/core#memberList";
        /**
         * skos:narrowMatch is used to state a hierarchical mapping link between two conceptual resources in different concept schemes.
         */
        public static final String narrowMatch = "http://www.w3.org/2004/02/skos/core#narrowMatch";
        /**
         * Relates a concept to a concept that is more specific in meaning.
         */
        public static final String narrower = "http://www.w3.org/2004/02/skos/core#narrower";
        /**
         * skos:narrowerTransitive is a transitive superproperty of skos:narrower.
         */
        public static final String narrowerTransitive = "http://www.w3.org/2004/02/skos/core#narrowerTransitive";
        /**
         * A notation, also known as classification code, is a string of characters such as "T58.5" or "303.4833" used to uniquely identify a concept within the scope of a given concept scheme.
         */
        public static final String notation = "http://www.w3.org/2004/02/skos/core#notation";
        /**
         * A general note, for any purpose.
         */
        public static final String note = "http://www.w3.org/2004/02/skos/core#note";
        /**
         * The preferred lexical label for a resource, in a given language.
         */
        public static final String prefLabel = "http://www.w3.org/2004/02/skos/core#prefLabel";
        /**
         * Relates a concept to a concept with which there is an associative semantic relationship.
         */
        public static final String related = "http://www.w3.org/2004/02/skos/core#related";
        /**
         * skos:relatedMatch is used to state an associative mapping link between two conceptual resources in different concept schemes.
         */
        public static final String relatedMatch = "http://www.w3.org/2004/02/skos/core#relatedMatch";
        /**
         * A note that helps to clarify the meaning and/or the use of a concept.
         */
        public static final String scopeNote = "http://www.w3.org/2004/02/skos/core#scopeNote";
        /**
         * Links a concept to a concept related by meaning.
         */
        public static final String semanticRelation = "http://www.w3.org/2004/02/skos/core#semanticRelation";
        /**
         * Relates a concept to the concept scheme that it is a top level concept of.
         */
        public static final String topConceptOf = "http://www.w3.org/2004/02/skos/core#topConceptOf";

        /**
         * Returns baseURI for namespace SKOS
         * @return baseUri
         */
        public static final String baseURI(){
            return "http://www.w3.org/2004/02/skos/core#";
        }
    }

    /**
     * namespace DCTERMS
     */
    public static class DCTERMS {
        /**
         * A resource that acts or has the power to act.
         */
        public static final String Agent = "http://purl.org/dc/terms/Agent";
        /**
         * A group of agents.
         */
        public static final String AgentClass = "http://purl.org/dc/terms/AgentClass";
        /**
         * A book, article, or other documentary resource.
         */
        public static final String BibliographicResource = "http://purl.org/dc/terms/BibliographicResource";
        /**
         * The set of regions in space defined by their geographic coordinates according to the DCMI Box Encoding Scheme.
         */
        public static final String Box = "http://purl.org/dc/terms/Box";
        /**
         * The set of classes specified by the DCMI Type Vocabulary, used to categorize the nature or genre of the resource.
         */
        public static final String DCMIType = "http://purl.org/dc/terms/DCMIType";
        /**
         * The set of conceptual resources specified by the Dewey Decimal Classification.
         */
        public static final String DDC = "http://purl.org/dc/terms/DDC";
        /**
         * A digital resource format.
         */
        public static final String FileFormat = "http://purl.org/dc/terms/FileFormat";
        /**
         * A rate at which something recurs.
         */
        public static final String Frequency = "http://purl.org/dc/terms/Frequency";
        /**
         * The set of media types specified by the Internet Assigned Numbers Authority.
         */
        public static final String IMT = "http://purl.org/dc/terms/IMT";
        /**
         * The set of codes listed in ISO 3166-1 for the representation of names of countries.
         */
        public static final String ISO3166 = "http://purl.org/dc/terms/ISO3166";
        /**
         * The three-letter alphabetic codes listed in ISO639-2 for the representation of names of languages.
         */
        public static final String ISO639_2 = "http://purl.org/dc/terms/ISO639-2";
        /**
         * The set of three-letter codes listed in ISO 639-3 for the representation of names of languages.
         */
        public static final String ISO639_3 = "http://purl.org/dc/terms/ISO639-3";
        /**
         * The extent or range of judicial, law enforcement, or other authority.
         */
        public static final String Jurisdiction = "http://purl.org/dc/terms/Jurisdiction";
        /**
         * The set of conceptual resources specified by the Library of Congress Classification.
         */
        public static final String LCC = "http://purl.org/dc/terms/LCC";
        /**
         * The set of labeled concepts specified by the Library of Congress Subject Headings.
         */
        public static final String LCSH = "http://purl.org/dc/terms/LCSH";
        /**
         * A legal document giving official permission to do something with a Resource.
         */
        public static final String LicenseDocument = "http://purl.org/dc/terms/LicenseDocument";
        /**
         * A system of signs, symbols, sounds, gestures, or rules used in communication.
         */
        public static final String LinguisticSystem = "http://purl.org/dc/terms/LinguisticSystem";
        /**
         * A spatial region or named place.
         */
        public static final String Location = "http://purl.org/dc/terms/Location";
        /**
         * A location, period of time, or jurisdiction.
         */
        public static final String LocationPeriodOrJurisdiction = "http://purl.org/dc/terms/LocationPeriodOrJurisdiction";
        /**
         * The set of labeled concepts specified by the Medical Subject Headings.
         */
        public static final String MESH = "http://purl.org/dc/terms/MESH";
        /**
         * A file format or physical medium.
         */
        public static final String MediaType = "http://purl.org/dc/terms/MediaType";
        /**
         * A media type or extent.
         */
        public static final String MediaTypeOrExtent = "http://purl.org/dc/terms/MediaTypeOrExtent";
        /**
         * A method by which resources are added to a collection.
         */
        public static final String MethodOfAccrual = "http://purl.org/dc/terms/MethodOfAccrual";
        /**
         * A process that is used to engender knowledge, attitudes, and skills.
         */
        public static final String MethodOfInstruction = "http://purl.org/dc/terms/MethodOfInstruction";
        /**
         * The set of conceptual resources specified by the National Library of Medicine Classification.
         */
        public static final String NLM = "http://purl.org/dc/terms/NLM";
        /**
         * The set of time intervals defined by their limits according to the DCMI Period Encoding Scheme.
         */
        public static final String Period = "http://purl.org/dc/terms/Period";
        /**
         * An interval of time that is named or defined by its start and end dates.
         */
        public static final String PeriodOfTime = "http://purl.org/dc/terms/PeriodOfTime";
        /**
         * A physical material or carrier.
         */
        public static final String PhysicalMedium = "http://purl.org/dc/terms/PhysicalMedium";
        /**
         * A material thing.
         */
        public static final String PhysicalResource = "http://purl.org/dc/terms/PhysicalResource";
        /**
         * The set of points in space defined by their geographic coordinates according to the DCMI Point Encoding Scheme.
         */
        public static final String Point = "http://purl.org/dc/terms/Point";
        /**
         * A plan or course of action by an authority, intended to influence and determine decisions, actions, and other matters.
         */
        public static final String Policy = "http://purl.org/dc/terms/Policy";
        /**
         * A statement of any changes in ownership and custody of a resource since its creation that are significant for its authenticity, integrity, and interpretation.
         */
        public static final String ProvenanceStatement = "http://purl.org/dc/terms/ProvenanceStatement";
        /**
         * The set of tags, constructed according to RFC 1766, for the identification of languages.
         */
        public static final String RFC1766 = "http://purl.org/dc/terms/RFC1766";
        /**
         * The set of tags constructed according to RFC 3066 for the identification of languages.
         */
        public static final String RFC3066 = "http://purl.org/dc/terms/RFC3066";
        /**
         * The set of tags constructed according to RFC 4646 for the identification of languages.
         */
        public static final String RFC4646 = "http://purl.org/dc/terms/RFC4646";
        /**
         * A statement about the intellectual property rights (IPR) held in or over a Resource, a legal document giving official permission to do something with a resource, or a statement about access rights.
         */
        public static final String RightsStatement = "http://purl.org/dc/terms/RightsStatement";
        /**
         * A dimension or extent, or a time taken to play or execute.
         */
        public static final String SizeOrDuration = "http://purl.org/dc/terms/SizeOrDuration";
        /**
         * A basis for comparison; a reference point against which other things can be evaluated.
         */
        public static final String Standard = "http://purl.org/dc/terms/Standard";
        /**
         * The set of places specified by the Getty Thesaurus of Geographic Names.
         */
        public static final String TGN = "http://purl.org/dc/terms/TGN";
        /**
         * The set of conceptual resources specified by the Universal Decimal Classification.
         */
        public static final String UDC = "http://purl.org/dc/terms/UDC";
        /**
         * The set of identifiers constructed according to the generic syntax for Uniform Resource Identifiers as specified by the Internet Engineering Task Force.
         */
        public static final String URI = "http://purl.org/dc/terms/URI";
        /**
         * The set of dates and times constructed according to the W3C Date and Time Formats Specification.
         */
        public static final String W3CDTF = "http://purl.org/dc/terms/W3CDTF";
        /**
         * A summary of the resource.
         */
        public static final String abstract_ = "http://purl.org/dc/terms/abstract";
        /**
         * Information about who can access the resource or an indication of its security status.
         */
        public static final String accessRights = "http://purl.org/dc/terms/accessRights";
        /**
         * The method by which items are added to a collection.
         */
        public static final String accrualMethod = "http://purl.org/dc/terms/accrualMethod";
        /**
         * The frequency with which items are added to a collection.
         */
        public static final String accrualPeriodicity = "http://purl.org/dc/terms/accrualPeriodicity";
        /**
         * The policy governing the addition of items to a collection.
         */
        public static final String accrualPolicy = "http://purl.org/dc/terms/accrualPolicy";
        /**
         * An alternative name for the resource.
         */
        public static final String alternative = "http://purl.org/dc/terms/alternative";
        /**
         * A class of entity for whom the resource is intended or useful.
         */
        public static final String audience = "http://purl.org/dc/terms/audience";
        /**
         * Date (often a range) that the resource became or will become available.
         */
        public static final String available = "http://purl.org/dc/terms/available";
        /**
         * A bibliographic reference for the resource.
         */
        public static final String bibliographicCitation = "http://purl.org/dc/terms/bibliographicCitation";
        /**
         * An established standard to which the described resource conforms.
         */
        public static final String conformsTo = "http://purl.org/dc/terms/conformsTo";
        /**
         * An entity responsible for making contributions to the resource.
         */
        public static final String contributor = "http://purl.org/dc/terms/contributor";
        /**
         * The spatial or temporal topic of the resource, the spatial applicability of the resource, or the jurisdiction under which the resource is relevant.
         */
        public static final String coverage = "http://purl.org/dc/terms/coverage";
        /**
         * Date of creation of the resource.
         */
        public static final String created = "http://purl.org/dc/terms/created";
        /**
         * An entity primarily responsible for making the resource.
         */
        public static final String creator = "http://purl.org/dc/terms/creator";
        /**
         * A point or period of time associated with an event in the lifecycle of the resource.
         */
        public static final String date = "http://purl.org/dc/terms/date";
        /**
         * Date of acceptance of the resource.
         */
        public static final String dateAccepted = "http://purl.org/dc/terms/dateAccepted";
        /**
         * Date of copyright.
         */
        public static final String dateCopyrighted = "http://purl.org/dc/terms/dateCopyrighted";
        /**
         * Date of submission of the resource.
         */
        public static final String dateSubmitted = "http://purl.org/dc/terms/dateSubmitted";
        /**
         * An account of the resource.
         */
        public static final String description = "http://purl.org/dc/terms/description";
        /**
         * A class of entity, defined in terms of progression through an educational or training context, for which the described resource is intended.
         */
        public static final String educationLevel = "http://purl.org/dc/terms/educationLevel";
        /**
         * The size or duration of the resource.
         */
        public static final String extent = "http://purl.org/dc/terms/extent";
        /**
         * The file format, physical medium, or dimensions of the resource.
         */
        public static final String format = "http://purl.org/dc/terms/format";
        /**
         * A related resource that is substantially the same as the pre-existing described resource, but in another format.
         */
        public static final String hasFormat = "http://purl.org/dc/terms/hasFormat";
        /**
         * A related resource that is included either physically or logically in the described resource.
         */
        public static final String hasPart = "http://purl.org/dc/terms/hasPart";
        /**
         * A related resource that is a version, edition, or adaptation of the described resource.
         */
        public static final String hasVersion = "http://purl.org/dc/terms/hasVersion";
        /**
         * An unambiguous reference to the resource within a given context.
         */
        public static final String identifier = "http://purl.org/dc/terms/identifier";
        /**
         * A process, used to engender knowledge, attitudes and skills, that the described resource is designed to support.
         */
        public static final String instructionalMethod = "http://purl.org/dc/terms/instructionalMethod";
        /**
         * A related resource that is substantially the same as the described resource, but in another format.
         */
        public static final String isFormatOf = "http://purl.org/dc/terms/isFormatOf";
        /**
         * A related resource in which the described resource is physically or logically included.
         */
        public static final String isPartOf = "http://purl.org/dc/terms/isPartOf";
        /**
         * A related resource that references, cites, or otherwise points to the described resource.
         */
        public static final String isReferencedBy = "http://purl.org/dc/terms/isReferencedBy";
        /**
         * A related resource that supplants, displaces, or supersedes the described resource.
         */
        public static final String isReplacedBy = "http://purl.org/dc/terms/isReplacedBy";
        /**
         * A related resource that requires the described resource to support its function, delivery, or coherence.
         */
        public static final String isRequiredBy = "http://purl.org/dc/terms/isRequiredBy";
        /**
         * A related resource of which the described resource is a version, edition, or adaptation.
         */
        public static final String isVersionOf = "http://purl.org/dc/terms/isVersionOf";
        /**
         * Date of formal issuance (e.g., publication) of the resource.
         */
        public static final String issued = "http://purl.org/dc/terms/issued";
        /**
         * A language of the resource.
         */
        public static final String language = "http://purl.org/dc/terms/language";
        /**
         * A legal document giving official permission to do something with the resource.
         */
        public static final String license = "http://purl.org/dc/terms/license";
        /**
         * An entity that mediates access to the resource and for whom the resource is intended or useful.
         */
        public static final String mediator = "http://purl.org/dc/terms/mediator";
        /**
         * The material or physical carrier of the resource.
         */
        public static final String medium = "http://purl.org/dc/terms/medium";
        /**
         * Date on which the resource was changed.
         */
        public static final String modified = "http://purl.org/dc/terms/modified";
        /**
         * A statement of any changes in ownership and custody of the resource since its creation that are significant for its authenticity, integrity, and interpretation.
         */
        public static final String provenance = "http://purl.org/dc/terms/provenance";
        /**
         * An entity responsible for making the resource available.
         */
        public static final String publisher = "http://purl.org/dc/terms/publisher";
        /**
         * A related resource that is referenced, cited, or otherwise pointed to by the described resource.
         */
        public static final String references = "http://purl.org/dc/terms/references";
        /**
         * A related resource.
         */
        public static final String relation = "http://purl.org/dc/terms/relation";
        /**
         * A related resource that is supplanted, displaced, or superseded by the described resource.
         */
        public static final String replaces = "http://purl.org/dc/terms/replaces";
        /**
         * A related resource that is required by the described resource to support its function, delivery, or coherence.
         */
        public static final String requires = "http://purl.org/dc/terms/requires";
        /**
         * Information about rights held in and over the resource.
         */
        public static final String rights = "http://purl.org/dc/terms/rights";
        /**
         * A person or organization owning or managing rights over the resource.
         */
        public static final String rightsHolder = "http://purl.org/dc/terms/rightsHolder";
        /**
         * A related resource from which the described resource is derived.
         */
        public static final String source = "http://purl.org/dc/terms/source";
        /**
         * Spatial characteristics of the resource.
         */
        public static final String spatial = "http://purl.org/dc/terms/spatial";
        /**
         * The topic of the resource.
         */
        public static final String subject = "http://purl.org/dc/terms/subject";
        /**
         * A list of subunits of the resource.
         */
        public static final String tableOfContents = "http://purl.org/dc/terms/tableOfContents";
        /**
         * Temporal characteristics of the resource.
         */
        public static final String temporal = "http://purl.org/dc/terms/temporal";
        /**
         * A name given to the resource
         */
        public static final String title = "http://purl.org/dc/terms/title";
        /**
         * The nature or genre of the resource.
         */
        public static final String type = "http://purl.org/dc/terms/type";
        /**
         * Date (often a range) of validity of a resource.
         */
        public static final String valid = "http://purl.org/dc/terms/valid";

        /**
         * Returns baseURI for namespace DCTERMS
         * @return baseUri
         */
        public static final String baseURI(){
            return "http://purl.org/dc/terms/";
        }
    }

    /**
     * Namespace FOAF
     */
    public static class FOAF {
        /**
         * An agent (eg. person, group, software or physical artifact).
         */
        public static final String Agent = "http://xmlns.com/foaf/0.1/Agent";
        /**
         * A document.
         */
        public static final String Document = "http://xmlns.com/foaf/0.1/Document";
        /**
         * A class of Agents.
         */
        public static final String Group = "http://xmlns.com/foaf/0.1/Group";
        /**
         * An image.
         */
        public static final String Image = "http://xmlns.com/foaf/0.1/Image";
        /**
         * A foaf:LabelProperty is any RDF property with texual values that serve as labels.
         */
        public static final String LabelProperty = "http://xmlns.com/foaf/0.1/LabelProperty";
        /**
         * An online account.
         */
        public static final String OnlineAccount = "http://xmlns.com/foaf/0.1/OnlineAccount";
        /**
         * An online chat account.
         */
        public static final String OnlineChatAccount = "http://xmlns.com/foaf/0.1/OnlineChatAccount";
        /**
         * An online e-commerce account.
         */
        public static final String OnlineEcommerceAccount = "http://xmlns.com/foaf/0.1/OnlineEcommerceAccount";
        /**
         * An online gaming account.
         */
        public static final String OnlineGamingAccount = "http://xmlns.com/foaf/0.1/OnlineGamingAccount";
        /**
         * An organization.
         */
        public static final String Organization = "http://xmlns.com/foaf/0.1/Organization";
        /**
         * A person.
         */
        public static final String Person = "http://xmlns.com/foaf/0.1/Person";
        /**
         * A personal profile RDF document.
         */
        public static final String PersonalProfileDocument = "http://xmlns.com/foaf/0.1/PersonalProfileDocument";
        /**
         * A project (a collective endeavour of some kind).
         */
        public static final String Project = "http://xmlns.com/foaf/0.1/Project";
        /**
         * Indicates an account held by this agent.
         */
        public static final String account = "http://xmlns.com/foaf/0.1/account";
        /**
         * Indicates the name (identifier) associated with this online account.
         */
        public static final String accountName = "http://xmlns.com/foaf/0.1/accountName";
        /**
         * Indicates a homepage of the service provide for this online account.
         */
        public static final String accountServiceHomepage = "http://xmlns.com/foaf/0.1/accountServiceHomepage";
        /**
         * The age in years of some agent.
         */
        public static final String age = "http://xmlns.com/foaf/0.1/age";
        /**
         * An AIM chat ID
         */
        public static final String aimChatID = "http://xmlns.com/foaf/0.1/aimChatID";
        /**
         * A location that something is based near, for some broadly human notion of near.
         */
        public static final String based_near = "http://xmlns.com/foaf/0.1/based_near";
        /**
         * The birthday of this Agent, represented in mm-dd string form, eg. '12-31'.
         */
        public static final String birthday = "http://xmlns.com/foaf/0.1/birthday";
        /**
         * A current project this person works on.
         */
        public static final String currentProject = "http://xmlns.com/foaf/0.1/currentProject";
        /**
         * A depiction of some thing.
         */
        public static final String depiction = "http://xmlns.com/foaf/0.1/depiction";
        /**
         * A thing depicted in this representation.
         */
        public static final String depicts = "http://xmlns.com/foaf/0.1/depicts";
        /**
         * A checksum for the DNA of some thing. Joke.
         */
        public static final String dnaChecksum = "http://xmlns.com/foaf/0.1/dnaChecksum";
        /**
         * The family name of some person.
         */
        public static final String familyName = "http://xmlns.com/foaf/0.1/familyName";
        /**
         * The family name of some person.
         */
        public static final String family_name = "http://xmlns.com/foaf/0.1/family_name";
        /**
         * The first name of a person.
         */
        public static final String firstName = "http://xmlns.com/foaf/0.1/firstName";
        /**
         * The underlying or 'focal' entity associated with some SKOS-described concept.
         */
        public static final String focus = "http://xmlns.com/foaf/0.1/focus";
        /**
         * An organization funding a project or person.
         */
        public static final String fundedBy = "http://xmlns.com/foaf/0.1/fundedBy";
        /**
         * A textual geekcode for this person, see http://www.geekcode.com/geek.html
         */
        public static final String geekcode = "http://xmlns.com/foaf/0.1/geekcode";
        /**
         * The gender of this Agent (typically but not necessarily 'male' or 'female').
         */
        public static final String gender = "http://xmlns.com/foaf/0.1/gender";
        /**
         * The given name of some person.
         */
        public static final String givenName = "http://xmlns.com/foaf/0.1/givenName";
        /**
         * The given name of some person.
         */
        public static final String givenname = "http://xmlns.com/foaf/0.1/givenname";
        /**
         * Indicates an account held by this agent.
         */
        public static final String holdsAccount = "http://xmlns.com/foaf/0.1/holdsAccount";
        /**
         * A homepage for some thing.
         */
        public static final String homepage = "http://xmlns.com/foaf/0.1/homepage";
        /**
         * An ICQ chat ID
         */
        public static final String icqChatID = "http://xmlns.com/foaf/0.1/icqChatID";
        /**
         * An image that can be used to represent some thing (ie. those depictions which are particularly representative of something, eg. one's photo on a homepage).
         */
        public static final String img = "http://xmlns.com/foaf/0.1/img";
        /**
         * A page about a topic of interest to this person.
         */
        public static final String interest = "http://xmlns.com/foaf/0.1/interest";
        /**
         * A document that this thing is the primary topic of.
         */
        public static final String isPrimaryTopicOf = "http://xmlns.com/foaf/0.1/isPrimaryTopicOf";
        /**
         * A jabber ID for something.
         */
        public static final String jabberID = "http://xmlns.com/foaf/0.1/jabberID";
        /**
         * A person known by this person (indicating some level of reciprocated interaction between the parties).
         */
        public static final String knows = "http://xmlns.com/foaf/0.1/knows";
        /**
         * The last name of a person.
         */
        public static final String lastName = "http://xmlns.com/foaf/0.1/lastName";
        /**
         * A logo representing some thing.
         */
        public static final String logo = "http://xmlns.com/foaf/0.1/logo";
        /**
         * Something that was made by this agent.
         */
        public static final String made = "http://xmlns.com/foaf/0.1/made";
        /**
         * An agent that  made this thing.
         */
        public static final String maker = "http://xmlns.com/foaf/0.1/maker";
        /**
         * A  personal mailbox, ie. an Internet mailbox associated with exactly one owner, the first owner of this mailbox. This is a 'static inverse functional property', in that  there is (across time and change) at most one individual that ever has any particular value for foaf:mbox.
         */
        public static final String mbox = "http://xmlns.com/foaf/0.1/mbox";
        /**
         * The sha1sum of the URI of an Internet mailbox associated with exactly one owner, the  first owner of the mailbox.
         */
        public static final String mbox_sha1sum = "http://xmlns.com/foaf/0.1/mbox_sha1sum";
        /**
         * Indicates a member of a Group
         */
        public static final String member = "http://xmlns.com/foaf/0.1/member";
        /**
         * Indicates the class of individuals that are a member of a Group
         */
        public static final String membershipClass = "http://xmlns.com/foaf/0.1/membershipClass";
        /**
         * An MSN chat ID
         */
        public static final String msnChatID = "http://xmlns.com/foaf/0.1/msnChatID";
        /**
         * A Myers Briggs (MBTI) personality classification.
         */
        public static final String myersBriggs = "http://xmlns.com/foaf/0.1/myersBriggs";
        /**
         * A name for some thing.
         */
        public static final String name = "http://xmlns.com/foaf/0.1/name";
        /**
         * A short informal nickname characterising an agent (includes login identifiers, IRC and other chat nicknames).
         */
        public static final String nick = "http://xmlns.com/foaf/0.1/nick";
        /**
         * An OpenID for an Agent.
         */
        public static final String openid = "http://xmlns.com/foaf/0.1/openid";
        /**
         * A page or document about this thing.
         */
        public static final String page = "http://xmlns.com/foaf/0.1/page";
        /**
         * A project this person has previously worked on.
         */
        public static final String pastProject = "http://xmlns.com/foaf/0.1/pastProject";
        /**
         * A phone,  specified using fully qualified tel: URI scheme (refs: http://www.w3.org/Addressing/schemes.html#tel).
         */
        public static final String phone = "http://xmlns.com/foaf/0.1/phone";
        /**
         * A .plan comment, in the tradition of finger and '.plan' files.
         */
        public static final String plan = "http://xmlns.com/foaf/0.1/plan";
        /**
         * The primary topic of some page or document.
         */
        public static final String primaryTopic = "http://xmlns.com/foaf/0.1/primaryTopic";
        /**
         * A link to the publications of this person.
         */
        public static final String publications = "http://xmlns.com/foaf/0.1/publications";
        /**
         * A homepage of a school attended by the person.
         */
        public static final String schoolHomepage = "http://xmlns.com/foaf/0.1/schoolHomepage";
        /**
         * A sha1sum hash, in hex.
         */
        public static final String sha1 = "http://xmlns.com/foaf/0.1/sha1";
        /**
         * A Skype ID
         */
        public static final String skypeID = "http://xmlns.com/foaf/0.1/skypeID";
        /**
         * A string expressing what the user is happy for the general public (normally) to know about their current activity.
         */
        public static final String status = "http://xmlns.com/foaf/0.1/status";
        /**
         * The surname of some person.
         */
        public static final String surname = "http://xmlns.com/foaf/0.1/surname";
        /**
         * A theme.
         */
        public static final String theme = "http://xmlns.com/foaf/0.1/theme";
        /**
         * A derived thumbnail image.
         */
        public static final String thumbnail = "http://xmlns.com/foaf/0.1/thumbnail";
        /**
         * A tipjar document for this agent, describing means for payment and reward.
         */
        public static final String tipjar = "http://xmlns.com/foaf/0.1/tipjar";
        /**
         * Title (Mr, Mrs, Ms, Dr. etc)
         */
        public static final String title = "http://xmlns.com/foaf/0.1/title";
        /**
         * A topic of some page or document.
         */
        public static final String topic = "http://xmlns.com/foaf/0.1/topic";
        /**
         * A thing of interest to this person.
         */
        public static final String topic_interest = "http://xmlns.com/foaf/0.1/topic_interest";
        /**
         * A weblog of some thing (whether person, group, company etc.).
         */
        public static final String weblog = "http://xmlns.com/foaf/0.1/weblog";
        /**
         * A work info homepage of some person; a page about their work for some organization.
         */
        public static final String workInfoHomepage = "http://xmlns.com/foaf/0.1/workInfoHomepage";
        /**
         * A workplace homepage of some person; the homepage of an organization they work for.
         */
        public static final String workplaceHomepage = "http://xmlns.com/foaf/0.1/workplaceHomepage";
        /**
         * A Yahoo chat ID
         */
        public static final String yahooChatID = "http://xmlns.com/foaf/0.1/yahooChatID";

        /**
         * Returns baseURI for namespace FOAF
         * @return baseUri
         */
        public static final String baseURI(){
            return "http://xmlns.com/foaf/0.1/";
        }
    }

    /**
     * Namespace OWL
     */
    public static class OWL {
        /**
         * The class of collections of pairwise different individuals.
         */
        public static final String AllDifferent = "http://www.w3.org/2002/07/owl#AllDifferent";
        /**
         * The class of collections of pairwise disjoint classes.
         */
        public static final String AllDisjointClasses = "http://www.w3.org/2002/07/owl#AllDisjointClasses";
        /**
         * The class of collections of pairwise disjoint properties.
         */
        public static final String AllDisjointProperties = "http://www.w3.org/2002/07/owl#AllDisjointProperties";
        /**
         * The class of annotated annotations for which the RDF serialization consists of an annotated subject, predicate and object.
         */
        public static final String Annotation = "http://www.w3.org/2002/07/owl#Annotation";
        /**
         * The class of annotation properties.
         */
        public static final String AnnotationProperty = "http://www.w3.org/2002/07/owl#AnnotationProperty";
        /**
         * The class of asymmetric properties.
         */
        public static final String AsymmetricProperty = "http://www.w3.org/2002/07/owl#AsymmetricProperty";
        /**
         * The class of annotated axioms for which the RDF serialization consists of an annotated subject, predicate and object.
         */
        public static final String Axiom = "http://www.w3.org/2002/07/owl#Axiom";
        /**
         * The class of OWL classes.
         */
        public static final String Class = "http://www.w3.org/2002/07/owl#Class";
        /**
         * The class of OWL data ranges, which are special kinds of datatypes. Note: The use of the IRI owl:DataRange has been deprecated as of OWL 2. The IRI rdfs:Datatype SHOULD be used instead.
         */
        public static final String DataRange = "http://www.w3.org/2002/07/owl#DataRange";
        /**
         * The class of data properties.
         */
        public static final String DatatypeProperty = "http://www.w3.org/2002/07/owl#DatatypeProperty";
        /**
         * The class of deprecated classes.
         */
        public static final String DeprecatedClass = "http://www.w3.org/2002/07/owl#DeprecatedClass";
        /**
         * The class of deprecated properties.
         */
        public static final String DeprecatedProperty = "http://www.w3.org/2002/07/owl#DeprecatedProperty";
        /**
         * The class of functional properties.
         */
        public static final String FunctionalProperty = "http://www.w3.org/2002/07/owl#FunctionalProperty";
        /**
         * The class of inverse-functional properties.
         */
        public static final String InverseFunctionalProperty = "http://www.w3.org/2002/07/owl#InverseFunctionalProperty";
        /**
         * The class of irreflexive properties.
         */
        public static final String IrreflexiveProperty = "http://www.w3.org/2002/07/owl#IrreflexiveProperty";
        /**
         * The class of named individuals.
         */
        public static final String NamedIndividual = "http://www.w3.org/2002/07/owl#NamedIndividual";
        /**
         * The class of negative property assertions.
         */
        public static final String NegativePropertyAssertion = "http://www.w3.org/2002/07/owl#NegativePropertyAssertion";
        /**
         * This is the empty class.
         */
        public static final String Nothing = "http://www.w3.org/2002/07/owl#Nothing";
        /**
         * The class of object properties.
         */
        public static final String ObjectProperty = "http://www.w3.org/2002/07/owl#ObjectProperty";
        /**
         * The class of ontologies.
         */
        public static final String Ontology = "http://www.w3.org/2002/07/owl#Ontology";
        /**
         * The class of ontology properties.
         */
        public static final String OntologyProperty = "http://www.w3.org/2002/07/owl#OntologyProperty";
        /**
         * The class of reflexive properties.
         */
        public static final String ReflexiveProperty = "http://www.w3.org/2002/07/owl#ReflexiveProperty";
        /**
         * The class of property restrictions.
         */
        public static final String Restriction = "http://www.w3.org/2002/07/owl#Restriction";
        /**
         * The class of symmetric properties.
         */
        public static final String SymmetricProperty = "http://www.w3.org/2002/07/owl#SymmetricProperty";
        /**
         * The class of OWL individuals.
         */
        public static final String Thing = "http://www.w3.org/2002/07/owl#Thing";
        /**
         * The class of transitive properties.
         */
        public static final String TransitiveProperty = "http://www.w3.org/2002/07/owl#TransitiveProperty";
        /**
         * The property that determines the class that a universal property restriction refers to.
         */
        public static final String allValuesFrom = "http://www.w3.org/2002/07/owl#allValuesFrom";
        /**
         * The property that determines the predicate of an annotated axiom or annotated annotation.
         */
        public static final String annotatedProperty = "http://www.w3.org/2002/07/owl#annotatedProperty";
        /**
         * The property that determines the subject of an annotated axiom or annotated annotation.
         */
        public static final String annotatedSource = "http://www.w3.org/2002/07/owl#annotatedSource";
        /**
         * The property that determines the object of an annotated axiom or annotated annotation.
         */
        public static final String annotatedTarget = "http://www.w3.org/2002/07/owl#annotatedTarget";
        /**
         * The property that determines the predicate of a negative property assertion.
         */
        public static final String assertionProperty = "http://www.w3.org/2002/07/owl#assertionProperty";
        /**
         * The annotation property that indicates that a given ontology is backward compatible with another ontology.
         */
        public static final String backwardCompatibleWith = "http://www.w3.org/2002/07/owl#backwardCompatibleWith";
        /**
         * The data property that does not relate any individual to any data value.
         */
        public static final String bottomDataProperty = "http://www.w3.org/2002/07/owl#bottomDataProperty";
        /**
         * The object property that does not relate any two individuals.
         */
        public static final String bottomObjectProperty = "http://www.w3.org/2002/07/owl#bottomObjectProperty";
        /**
         * The property that determines the cardinality of an exact cardinality restriction.
         */
        public static final String cardinality = "http://www.w3.org/2002/07/owl#cardinality";
        /**
         * The property that determines that a given class is the complement of another class.
         */
        public static final String complementOf = "http://www.w3.org/2002/07/owl#complementOf";
        /**
         * The property that determines that a given data range is the complement of another data range with respect to the data domain.
         */
        public static final String datatypeComplementOf = "http://www.w3.org/2002/07/owl#datatypeComplementOf";
        /**
         * The annotation property that indicates that a given entity has been deprecated.
         */
        public static final String deprecated = "http://www.w3.org/2002/07/owl#deprecated";
        /**
         * The property that determines that two given individuals are different.
         */
        public static final String differentFrom = "http://www.w3.org/2002/07/owl#differentFrom";
        /**
         * The property that determines that a given class is equivalent to the disjoint union of a collection of other classes.
         */
        public static final String disjointUnionOf = "http://www.w3.org/2002/07/owl#disjointUnionOf";
        /**
         * The property that determines that two given classes are disjoint.
         */
        public static final String disjointWith = "http://www.w3.org/2002/07/owl#disjointWith";
        /**
         * The property that determines the collection of pairwise different individuals in a owl:AllDifferent axiom.
         */
        public static final String distinctMembers = "http://www.w3.org/2002/07/owl#distinctMembers";
        /**
         * The property that determines that two given classes are equivalent, and that is used to specify datatype definitions.
         */
        public static final String equivalentClass = "http://www.w3.org/2002/07/owl#equivalentClass";
        /**
         * The property that determines that two given properties are equivalent.
         */
        public static final String equivalentProperty = "http://www.w3.org/2002/07/owl#equivalentProperty";
        /**
         * The property that determines the collection of properties that jointly build a key.
         */
        public static final String hasKey = "http://www.w3.org/2002/07/owl#hasKey";
        /**
         * The property that determines the property that a self restriction refers to.
         */
        public static final String hasSelf = "http://www.w3.org/2002/07/owl#hasSelf";
        /**
         * The property that determines the individual that a has-value restriction refers to.
         */
        public static final String hasValue = "http://www.w3.org/2002/07/owl#hasValue";
        /**
         * The property that is used for importing other ontologies into a given ontology.
         */
        public static final String imports = "http://www.w3.org/2002/07/owl#imports";
        /**
         * The annotation property that indicates that a given ontology is incompatible with another ontology.
         */
        public static final String incompatibleWith = "http://www.w3.org/2002/07/owl#incompatibleWith";
        /**
         * The property that determines the collection of classes or data ranges that build an intersection.
         */
        public static final String intersectionOf = "http://www.w3.org/2002/07/owl#intersectionOf";
        /**
         * The property that determines that two given properties are inverse.
         */
        public static final String inverseOf = "http://www.w3.org/2002/07/owl#inverseOf";
        /**
         * The property that determines the cardinality of a maximum cardinality restriction.
         */
        public static final String maxCardinality = "http://www.w3.org/2002/07/owl#maxCardinality";
        /**
         * The property that determines the cardinality of a maximum qualified cardinality restriction.
         */
        public static final String maxQualifiedCardinality = "http://www.w3.org/2002/07/owl#maxQualifiedCardinality";
        /**
         * The property that determines the collection of members in either a owl:AllDifferent, owl:AllDisjointClasses or owl:AllDisjointProperties axiom.
         */
        public static final String members = "http://www.w3.org/2002/07/owl#members";
        /**
         * The property that determines the cardinality of a minimum cardinality restriction.
         */
        public static final String minCardinality = "http://www.w3.org/2002/07/owl#minCardinality";
        /**
         * The property that determines the cardinality of a minimum qualified cardinality restriction.
         */
        public static final String minQualifiedCardinality = "http://www.w3.org/2002/07/owl#minQualifiedCardinality";
        /**
         * The property that determines the class that a qualified object cardinality restriction refers to.
         */
        public static final String onClass = "http://www.w3.org/2002/07/owl#onClass";
        /**
         * The property that determines the data range that a qualified data cardinality restriction refers to.
         */
        public static final String onDataRange = "http://www.w3.org/2002/07/owl#onDataRange";
        /**
         * The property that determines the datatype that a datatype restriction refers to.
         */
        public static final String onDatatype = "http://www.w3.org/2002/07/owl#onDatatype";
        /**
         * The property that determines the n-tuple of properties that a property restriction on an n-ary data range refers to.
         */
        public static final String onProperties = "http://www.w3.org/2002/07/owl#onProperties";
        /**
         * The property that determines the property that a property restriction refers to.
         */
        public static final String onProperty = "http://www.w3.org/2002/07/owl#onProperty";
        /**
         * The property that determines the collection of individuals or data values that build an enumeration.
         */
        public static final String oneOf = "http://www.w3.org/2002/07/owl#oneOf";
        /**
         * The annotation property that indicates the predecessor ontology of a given ontology.
         */
        public static final String priorVersion = "http://www.w3.org/2002/07/owl#priorVersion";
        /**
         * The property that determines the n-tuple of properties that build a sub property chain of a given property.
         */
        public static final String propertyChainAxiom = "http://www.w3.org/2002/07/owl#propertyChainAxiom";
        /**
         * The property that determines that two given properties are disjoint.
         */
        public static final String propertyDisjointWith = "http://www.w3.org/2002/07/owl#propertyDisjointWith";
        /**
         * The property that determines the cardinality of an exact qualified cardinality restriction.
         */
        public static final String qualifiedCardinality = "http://www.w3.org/2002/07/owl#qualifiedCardinality";
        /**
         * The property that determines that two given individuals are equal.
         */
        public static final String sameAs = "http://www.w3.org/2002/07/owl#sameAs";
        /**
         * The property that determines the class that an existential property restriction refers to.
         */
        public static final String someValuesFrom = "http://www.w3.org/2002/07/owl#someValuesFrom";
        /**
         * The property that determines the subject of a negative property assertion.
         */
        public static final String sourceIndividual = "http://www.w3.org/2002/07/owl#sourceIndividual";
        /**
         * The property that determines the object of a negative object property assertion.
         */
        public static final String targetIndividual = "http://www.w3.org/2002/07/owl#targetIndividual";
        /**
         * The property that determines the value of a negative data property assertion.
         */
        public static final String targetValue = "http://www.w3.org/2002/07/owl#targetValue";
        /**
         * The data property that relates every individual to every data value.
         */
        public static final String topDataProperty = "http://www.w3.org/2002/07/owl#topDataProperty";
        /**
         * The object property that relates every two individuals.
         */
        public static final String topObjectProperty = "http://www.w3.org/2002/07/owl#topObjectProperty";
        /**
         * The property that determines the collection of classes or data ranges that build a union.
         */
        public static final String unionOf = "http://www.w3.org/2002/07/owl#unionOf";
        /**
         * The property that identifies the version IRI of an ontology.
         */
        public static final String versionIRI = "http://www.w3.org/2002/07/owl#versionIRI";
        /**
         * The annotation property that provides version information for an ontology or another OWL construct.
         */
        public static final String versionInfo = "http://www.w3.org/2002/07/owl#versionInfo";
        /**
         * The property that determines the collection of facet-value pairs that define a datatype restriction.
         */
        public static final String withRestrictions = "http://www.w3.org/2002/07/owl#withRestrictions";

        /**
         * Returns baseURI for namespace OWL
         * @return baseUri
         */
        public static final String baseURI(){
            return "http://www.w3.org/2002/07/owl#";
        }
    }

    /**
     * Namespace RDF
     */
    public static class RDF {
        /**
         * The class of containers of alternatives.
         */
        public static final String Alt = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt";
        /**
         * The class of unordered containers.
         */
        public static final String Bag = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag";
        /**
         * The class of RDF Lists.
         */
        public static final String List = "http://www.w3.org/1999/02/22-rdf-syntax-ns#List";
        /**
         * The class of plain (i.e. untyped) literal values.
         */
        public static final String PlainLiteral = "http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral";
        /**
         * The class of RDF properties.
         */
        public static final String Property = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";
        /**
         * The class of ordered containers.
         */
        public static final String Seq = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq";
        /**
         * The class of RDF statements.
         */
        public static final String Statement = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement";
        /**
         * The class of XML literal values.
         */
        public static final String XMLLiteral = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
        /**
         * The first item in the subject RDF list.
         */
        public static final String first = "http://www.w3.org/1999/02/22-rdf-syntax-ns#first";
        /**
         * The empty list, with no items in it. If the rest of a list is nil then the list has no more items in it.
         */
        public static final String nil = "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil";
        /**
         * The object of the subject RDF statement.
         */
        public static final String object = "http://www.w3.org/1999/02/22-rdf-syntax-ns#object";
        /**
         * The predicate of the subject RDF statement.
         */
        public static final String predicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate";
        /**
         * The rest of the subject RDF list after the first item.
         */
        public static final String rest = "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest";
        /**
         * The subject of the subject RDF statement.
         */
        public static final String subject = "http://www.w3.org/1999/02/22-rdf-syntax-ns#subject";
        /**
         * The subject is an instance of a class.
         */
        public static final String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        /**
         * Idiomatic property used for structured values.
         */
        public static final String value = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value";

        /**
         * Returns baseURI for namespace RDF
         * @return baseUri
         */
        public static final String baseURI(){
            return "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        }
    }

    /**
     * Namespace RDFS
     */
    public static class RDFS {
        /**
         * The class of classes.
         */
        public static final String Class = "http://www.w3.org/2000/01/rdf-schema#Class";
        /**
         * The class of RDF containers.
         */
        public static final String Container = "http://www.w3.org/2000/01/rdf-schema#Container";
        /**
         * The class of container membership properties, rdf:_1, rdf:_2, ..., all of which are sub-properties of 'member'.
         */
        public static final String ContainerMembershipProperty = "http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty";
        /**
         * The class of RDF datatypes.
         */
        public static final String Datatype = "http://www.w3.org/2000/01/rdf-schema#Datatype";
        /**
         * The class of literal values, eg. textual strings and integers.
         */
        public static final String Literal = "http://www.w3.org/2000/01/rdf-schema#Literal";
        /**
         * The class resource, everything.
         */
        public static final String Resource = "http://www.w3.org/2000/01/rdf-schema#Resource";
        /**
         * A description of the subject resource.
         */
        public static final String comment = "http://www.w3.org/2000/01/rdf-schema#comment";
        /**
         * A domain of the subject property.
         */
        public static final String domain = "http://www.w3.org/2000/01/rdf-schema#domain";
        /**
         * The defininition of the subject resource.
         */
        public static final String isDefinedBy = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
        /**
         * A human-readable name for the subject.
         */
        public static final String label = "http://www.w3.org/2000/01/rdf-schema#label";
        /**
         * A member of the subject resource.
         */
        public static final String member = "http://www.w3.org/2000/01/rdf-schema#member";
        /**
         * A range of the subject property.
         */
        public static final String range = "http://www.w3.org/2000/01/rdf-schema#range";
        /**
         * Further information about the subject resource.
         */
        public static final String seeAlso = "http://www.w3.org/2000/01/rdf-schema#seeAlso";
        /**
         * The subject is a subclass of a class.
         */
        public static final String subClassOf = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
        /**
         * The subject is a subproperty of a property.
         */
        public static final String subPropertyOf = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";

        /**
         * Returns baseURI for namespace RDFS
         * @return baseUri
         */
        public static final String baseURI(){
            return "http://www.w3.org/2000/01/rdf-schema#";
        }
    }

    /**
     * Namespace SIOC
     */
    public static class SIOC {
        /**
         * Community is a high-level concept that defines an online community and what it consists of.
         */
        public static final String Community = "http://rdfs.org/sioc/ns#Community";
        /**
         * An area in which content Items are contained.
         */
        public static final String Container = "http://rdfs.org/sioc/ns#Container";
        /**
         * A discussion area on which Posts or entries are made.
         */
        public static final String Forum = "http://rdfs.org/sioc/ns#Forum";
        /**
         * An Item is something which can be in a Container.
         */
        public static final String Item = "http://rdfs.org/sioc/ns#Item";
        /**
         * An article or message that can be posted to a Forum.
         */
        public static final String Post = "http://rdfs.org/sioc/ns#Post";
        /**
         * A Role is a function of a UserAccount within a scope of a particular Forum, Site, etc.
         */
        public static final String Role = "http://rdfs.org/sioc/ns#Role";
        /**
         * A Site can be the location of an online community or set of communities, with UserAccounts and Usergroups creating Items in a set of Containers. It can be thought of as a web-accessible data Space.
         */
        public static final String Site = "http://rdfs.org/sioc/ns#Site";
        /**
         * A Space is a place where data resides, e.g. on a website, desktop, fileshare, etc.
         */
        public static final String Space = "http://rdfs.org/sioc/ns#Space";
        /**
         * A container for a series of threaded discussion Posts or Items.
         */
        public static final String Thread = "http://rdfs.org/sioc/ns#Thread";
        /**
         * UserAccount is now preferred. This is a deprecated class for a User in an online community site.
         */
        public static final String User = "http://rdfs.org/sioc/ns#User";
        /**
         * A user account in an online community site.
         */
        public static final String UserAccount = "http://rdfs.org/sioc/ns#UserAccount";
        /**
         * A set of UserAccounts whose owners have a common purpose or interest. Can be used for access control purposes.
         */
        public static final String Usergroup = "http://rdfs.org/sioc/ns#Usergroup";
        /**
         * Specifies that this Item is about a particular resource, e.g. a Post describing a book, hotel, etc.
         */
        public static final String about = "http://rdfs.org/sioc/ns#about";
        /**
         * Refers to the foaf:Agent or foaf:Person who owns this sioc:UserAccount.
         */
        public static final String account_of = "http://rdfs.org/sioc/ns#account_of";
        /**
         * Refers to who (e.g. a UserAccount, e-mail address, etc.) a particular Item is addressed to.
         */
        public static final String addressed_to = "http://rdfs.org/sioc/ns#addressed_to";
        /**
         * A Site that the UserAccount is an administrator of.
         */
        public static final String administrator_of = "http://rdfs.org/sioc/ns#administrator_of";
        /**
         * The URI of a file attached to an Item.
         */
        public static final String attachment = "http://rdfs.org/sioc/ns#attachment";
        /**
         * An image or depiction used to represent this UserAccount.
         */
        public static final String avatar = "http://rdfs.org/sioc/ns#avatar";
        /**
         * An Item that this Container contains.
         */
        public static final String container_of = "http://rdfs.org/sioc/ns#container_of";
        /**
         * The content of the Item in plain text format.
         */
        public static final String content = "http://rdfs.org/sioc/ns#content";
        /**
         * The encoded content of the Post, contained in CDATA areas.
         */
        public static final String content_encoded = "http://rdfs.org/sioc/ns#content_encoded";
        /**
         * When this was created, in ISO 8601 format.
         */
        public static final String created_at = "http://rdfs.org/sioc/ns#created_at";
        /**
         * A resource that the UserAccount is a creator of.
         */
        public static final String creator_of = "http://rdfs.org/sioc/ns#creator_of";
        /**
         * The content of the Post.
         */
        public static final String description = "http://rdfs.org/sioc/ns#description";
        /**
         * Links to a previous (older) revision of this Item or Post.
         */
        public static final String earlier_version = "http://rdfs.org/sioc/ns#earlier_version";
        /**
         * An electronic mail address of the UserAccount.
         */
        public static final String email = "http://rdfs.org/sioc/ns#email";
        /**
         * An electronic mail address of the UserAccount, encoded using SHA1.
         */
        public static final String email_sha1 = "http://rdfs.org/sioc/ns#email_sha1";
        /**
         * This links Items to embedded statements, facts and structured content.
         */
        public static final String embeds_knowledge = "http://rdfs.org/sioc/ns#embeds_knowledge";
        /**
         * A feed (e.g. RSS, Atom, etc.) pertaining to this resource (e.g. for a Forum, Site, UserAccount, etc.).
         */
        public static final String feed = "http://rdfs.org/sioc/ns#feed";
        /**
         * First (real) name of this User. Synonyms include given name or christian name.
         */
        public static final String first_name = "http://rdfs.org/sioc/ns#first_name";
        /**
         * Indicates that one UserAccount follows another UserAccount (e.g. for microblog posts or other content item updates).
         */
        public static final String follows = "http://rdfs.org/sioc/ns#follows";
        /**
         * A UserAccount that has this Role.
         */
        public static final String function_of = "http://rdfs.org/sioc/ns#function_of";
        /**
         * This property has been renamed. Use sioc:usergroup_of instead.
         */
        @Deprecated
        public static final String group_of = "http://rdfs.org/sioc/ns#group_of";
        /**
         * A UserAccount that is an administrator of this Site.
         */
        public static final String has_administrator = "http://rdfs.org/sioc/ns#has_administrator";
        /**
         * The Container to which this Item belongs.
         */
        public static final String has_container = "http://rdfs.org/sioc/ns#has_container";
        /**
         * This is the UserAccount that made this resource.
         */
        public static final String has_creator = "http://rdfs.org/sioc/ns#has_creator";
        /**
         * The discussion that is related to this Item.
         */
        public static final String has_discussion = "http://rdfs.org/sioc/ns#has_discussion";
        /**
         * A Role that this UserAccount has.
         */
        public static final String has_function = "http://rdfs.org/sioc/ns#has_function";
        /**
         * This property has been renamed. Use sioc:has_usergroup instead.
         */
        @Deprecated
        public static final String has_group = "http://rdfs.org/sioc/ns#has_group";
        /**
         * The Site that hosts this Forum.
         */
        public static final String has_host = "http://rdfs.org/sioc/ns#has_host";
        /**
         * A UserAccount that is a member of this Usergroup.
         */
        public static final String has_member = "http://rdfs.org/sioc/ns#has_member";
        /**
         * A UserAccount that is a moderator of this Forum.
         */
        public static final String has_moderator = "http://rdfs.org/sioc/ns#has_moderator";
        /**
         * A UserAccount that modified this Item.
         */
        public static final String has_modifier = "http://rdfs.org/sioc/ns#has_modifier";
        /**
         * A UserAccount that this resource is owned by.
         */
        public static final String has_owner = "http://rdfs.org/sioc/ns#has_owner";
        /**
         * A Container or Forum that this Container or Forum is a child of.
         */
        public static final String has_parent = "http://rdfs.org/sioc/ns#has_parent";
        /**
         * An resource that is a part of this subject.
         */
        public static final String has_part = "http://rdfs.org/sioc/ns#has_part";
        /**
         * Points to an Item or Post that is a reply or response to this Item or Post.
         */
        public static final String has_reply = "http://rdfs.org/sioc/ns#has_reply";
        /**
         * A resource that this Role applies to.
         */
        public static final String has_scope = "http://rdfs.org/sioc/ns#has_scope";
        /**
         * A data Space which this resource is a part of.
         */
        public static final String has_space = "http://rdfs.org/sioc/ns#has_space";
        /**
         * A UserAccount that is subscribed to this Container.
         */
        public static final String has_subscriber = "http://rdfs.org/sioc/ns#has_subscriber";
        /**
         * Points to a Usergroup that has certain access to this Space.
         */
        public static final String has_usergroup = "http://rdfs.org/sioc/ns#has_usergroup";
        /**
         * A Forum that is hosted on this Site.
         */
        public static final String host_of = "http://rdfs.org/sioc/ns#host_of";
        /**
         * An identifier of a SIOC concept instance. For example, a user ID. Must be unique for instances of each type of SIOC concept within the same site.
         */
        public static final String id = "http://rdfs.org/sioc/ns#id";
        /**
         * The IP address used when creating this Item. This can be associated with a creator. Some wiki articles list the IP addresses for the creator or modifiers when the usernames are absent.
         */
        public static final String ip_address = "http://rdfs.org/sioc/ns#ip_address";
        /**
         * The date and time of the last activity associated with a SIOC concept instance, and expressed in ISO 8601 format. This could be due to a reply Post or Comment, a modification to an Item, etc.
         */
        public static final String last_activity_date = "http://rdfs.org/sioc/ns#last_activity_date";
        /**
         * The date and time of the last Post (or Item) in a Forum (or a Container), in ISO 8601 format.
         */
        public static final String last_item_date = "http://rdfs.org/sioc/ns#last_item_date";
        /**
         * Last (real) name of this user. Synonyms include surname or family name.
         */
        public static final String last_name = "http://rdfs.org/sioc/ns#last_name";
        /**
         * The date and time of the last reply Post or Comment, which could be associated with a starter Item or Post or with a Thread, and expressed in ISO 8601 format.
         */
        public static final String last_reply_date = "http://rdfs.org/sioc/ns#last_reply_date";
        /**
         * Links to a later (newer) revision of this Item or Post.
         */
        public static final String later_version = "http://rdfs.org/sioc/ns#later_version";
        /**
         * Links to the latest revision of this Item or Post.
         */
        public static final String latest_version = "http://rdfs.org/sioc/ns#latest_version";
        /**
         * A URI of a document which contains this SIOC object.
         */
        public static final String link = "http://rdfs.org/sioc/ns#link";
        /**
         * Links extracted from hyperlinks within a SIOC concept, e.g. Post or Site.
         */
        public static final String links_to = "http://rdfs.org/sioc/ns#links_to";
        /**
         * A Usergroup that this UserAccount is a member of.
         */
        public static final String member_of = "http://rdfs.org/sioc/ns#member_of";
        /**
         * A Forum that a UserAccount is a moderator of.
         */
        public static final String moderator_of = "http://rdfs.org/sioc/ns#moderator_of";
        /**
         * When this was modified, in ISO 8601 format.
         */
        public static final String modified_at = "http://rdfs.org/sioc/ns#modified_at";
        /**
         * An Item that this UserAccount has modified.
         */
        public static final String modifier_of = "http://rdfs.org/sioc/ns#modifier_of";
        /**
         * The name of a SIOC concept instance, e.g. a username for a UserAccount, group name for a Usergroup, etc.
         */
        public static final String name = "http://rdfs.org/sioc/ns#name";
        /**
         * Next Item or Post in a given Container sorted by date.
         */
        public static final String next_by_date = "http://rdfs.org/sioc/ns#next_by_date";
        /**
         * Links to the next revision of this Item or Post.
         */
        public static final String next_version = "http://rdfs.org/sioc/ns#next_version";
        /**
         * A note associated with this resource, for example, if it has been edited by a UserAccount.
         */
        public static final String note = "http://rdfs.org/sioc/ns#note";
        /**
         * The number of unique authors (UserAccounts and unregistered posters) who have contributed to this Item, Thread, Post, etc.
         */
        public static final String num_authors = "http://rdfs.org/sioc/ns#num_authors";
        /**
         * The number of Posts (or Items) in a Forum (or a Container).
         */
        public static final String num_items = "http://rdfs.org/sioc/ns#num_items";
        /**
         * The number of replies that this Item, Thread, Post, etc. has. Useful for when the reply structure is absent.
         */
        public static final String num_replies = "http://rdfs.org/sioc/ns#num_replies";
        /**
         * The number of Threads (AKA discussion topics) in a Forum.
         */
        public static final String num_threads = "http://rdfs.org/sioc/ns#num_threads";
        /**
         * The number of times this Item, Thread, UserAccount profile, etc. has been viewed.
         */
        public static final String num_views = "http://rdfs.org/sioc/ns#num_views";
        /**
         * A resource owned by a particular UserAccount, for example, a weblog or image gallery.
         */
        public static final String owner_of = "http://rdfs.org/sioc/ns#owner_of";
        /**
         * A child Container or Forum that this Container or Forum is a parent of.
         */
        public static final String parent_of = "http://rdfs.org/sioc/ns#parent_of";
        /**
         * A resource that the subject is a part of.
         */
        public static final String part_of = "http://rdfs.org/sioc/ns#part_of";
        /**
         * Previous Item or Post in a given Container sorted by date.
         */
        public static final String previous_by_date = "http://rdfs.org/sioc/ns#previous_by_date";
        /**
         * Links to the previous revision of this Item or Post.
         */
        public static final String previous_version = "http://rdfs.org/sioc/ns#previous_version";
        /**
         * Links either created explicitly or extracted implicitly on the HTML level from the Post.
         */
        public static final String reference = "http://rdfs.org/sioc/ns#reference";
        /**
         * Related Posts for this Post, perhaps determined implicitly from topics or references.
         */
        public static final String related_to = "http://rdfs.org/sioc/ns#related_to";
        /**
         * Links to an Item or Post which this Item or Post is a reply to.
         */
        public static final String reply_of = "http://rdfs.org/sioc/ns#reply_of";
        /**
         * A Role that has a scope of this resource.
         */
        public static final String scope_of = "http://rdfs.org/sioc/ns#scope_of";
        /**
         * An Item may have a sibling or a twin that exists in a different Container, but the siblings may differ in some small way (for example, language, category, etc.). The sibling of this Item should be self-describing (that is, it should contain all available information).
         */
        public static final String sibling = "http://rdfs.org/sioc/ns#sibling";
        /**
         * A resource which belongs to this data Space.
         */
        public static final String space_of = "http://rdfs.org/sioc/ns#space_of";
        /**
         * Keyword(s) describing subject of the Post.
         */
        public static final String subject = "http://rdfs.org/sioc/ns#subject";
        /**
         * A Container that a UserAccount is subscribed to.
         */
        public static final String subscriber_of = "http://rdfs.org/sioc/ns#subscriber_of";
        /**
         * This is the title (subject line) of the Post. Note that for a Post within a threaded discussion that has no parents, it would detail the topic thread.
         */
        public static final String title = "http://rdfs.org/sioc/ns#title";
        /**
         * A topic of interest, linking to the appropriate URI, e.g. in the Open Directory Project or of a SKOS category.
         */
        public static final String topic = "http://rdfs.org/sioc/ns#topic";
        /**
         * A Space that the Usergroup has access to.
         */
        public static final String usergroup_of = "http://rdfs.org/sioc/ns#usergroup_of";

        /**
         * Returns baseURI for namespace SIOC
         * @return baseUri
         */
        public static final String baseURI(){
            return "http://rdfs.org/sioc/ns#";
        }
    }

    /**
     * Namespace GEO
     */
    public static class GEO {
        /**
         * A point, typically described using a coordinate system relative to Earth, such as WGS84.
         */
        public static final String Point = "http://www.w3.org/2003/01/geo/wgs84_pos#Point";
        /**
         * Anything with spatial extent, i.e. size, shape, or position.
         * e.g. people, places, bowling balls, as well as abstract areas like cubes.
         */
        public static final String SpatialThing = "http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing";
        /**
         * The WGS84 altitude of a SpatialThing (decimal meters above the local reference ellipsoid).
         */
        public static final String alt = "http://www.w3.org/2003/01/geo/wgs84_pos#alt";
        /**
         * The WGS84 latitude of a SpatialThing (decimal degrees).
         */
        public static final String lat = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
        /**
         * A comma-separated representation of a latitude, longitude coordinate.
         */
        public static final String lat_long = "http://www.w3.org/2003/01/geo/wgs84_pos#lat_long";
        /**
         * The relation between something and the point,
         * or other geometrical thing in space, where it is.  For example, the realtionship between
         * a radio tower and a Point with a given lat and long.
         * Or a relationship between a park and its outline as a closed arc of points, or a road and
         * its location as a arc (a sequence of points).
         * Clearly in practice there will be limit to the accuracy of any such statement, but one would expect
         * an accuracy appropriate for the size of the object and uses such as mapping .
         */
        public static final String location = "http://www.w3.org/2003/01/geo/wgs84_pos#location";
        /**
         * The WGS84 longitude of a SpatialThing (decimal degrees).
         */
        public static final String long_ = "http://www.w3.org/2003/01/geo/wgs84_pos#long";

        /**
         * Returns baseURI for namespace GEO
         * @return baseUri
         */
        public static final String baseURI(){
            return "http://www.w3.org/2003/01/geo/wgs84_pos#";
        }
    }

    public static final String XSD                      = "http://www.w3.org/2001/XMLSchema#";
    public static final String XML                      = "http://www.w3.org/TR/2006/REC-xml11-20060816/#";
    public static final String XHTML                    = "http://www.w3.org/1999/xhtml";


}
