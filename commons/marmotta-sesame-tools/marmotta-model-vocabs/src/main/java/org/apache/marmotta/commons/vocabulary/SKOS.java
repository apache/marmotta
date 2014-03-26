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
 * Namespace SKOS
 */
public class SKOS {

    public static final String NAMESPACE = "http://www.w3.org/2004/02/skos/core#";

    public static final String PREFIX = "skos";

    /**
     * A meaningful collection of concepts.
     */
    public static final URI Collection;

    /**
     * An idea or notion; a unit of thought.
     */
    public static final URI Concept;

    /**
     * A set of concepts, optionally including statements about semantic relationships between those concepts.
     */
    public static final URI ConceptScheme;

    /**
     * An ordered collection of concepts, where both the grouping and the ordering are meaningful.
     */
    public static final URI OrderedCollection;

    /**
     * An alternative lexical label for a resource.
     */
    public static final URI altLabel;

    /**
     * skos:broadMatch is used to state a hierarchical mapping link between two conceptual resources in different concept schemes.
     */
    public static final URI broadMatch;

    /**
     * Relates a concept to a concept that is more general in meaning.
     */
    public static final URI broader;

    /**
     * skos:broaderTransitive is a transitive superproperty of skos:broader.
     */
    public static final URI broaderTransitive;

    /**
     * A note about a modification to a concept.
     */
    public static final URI changeNote;

    /**
     * skos:closeMatch is used to link two concepts that are sufficiently similar that they can be used interchangeably in some information retrieval applications. In order to avoid the possibility of "compound errors" when combining mappings across more than two concept schemes, skos:closeMatch is not declared to be a transitive property.
     */
    public static final URI closeMatch;

    /**
     * A statement or formal explanation of the meaning of a concept.
     */
    public static final URI definition;

    /**
     * A note for an editor, translator or maintainer of the vocabulary.
     */
    public static final URI editorialNote;

    /**
     * skos:exactMatch is used to link two concepts, indicating a high degree of confidence that the concepts can be used interchangeably across a wide range of information retrieval applications. skos:exactMatch is a transitive property, and is a sub-property of skos:closeMatch.
     */
    public static final URI exactMatch;

    /**
     * An example of the use of a concept.
     */
    public static final URI example;

    /**
     * Relates, by convention, a concept scheme to a concept which is topmost in the broader/narrower concept hierarchies for that scheme, providing an entry point to these hierarchies.
     */
    public static final URI hasTopConcept;

    /**
     * A lexical label for a resource that should be hidden when generating visual displays of the resource, but should still be accessible to free text search operations.
     */
    public static final URI hiddenLabel;

    /**
     * A note about the past state/use/meaning of a concept.
     */
    public static final URI historyNote;

    /**
     * Relates a resource (for example a concept) to a concept scheme in which it is included.
     */
    public static final URI inScheme;

    /**
     * Relates two concepts coming, by convention, from different schemes, and that have comparable meanings
     */
    public static final URI mappingRelation;

    /**
     * Relates a collection to one of its members.
     */
    public static final URI member;

    /**
     * Relates an ordered collection to the RDF list containing its members.
     */
    public static final URI memberList;

    /**
     * skos:narrowMatch is used to state a hierarchical mapping link between two conceptual resources in different concept schemes.
     */
    public static final URI narrowMatch;

    /**
     * Relates a concept to a concept that is more specific in meaning.
     */
    public static final URI narrower;

    /**
     * skos:narrowerTransitive is a transitive superproperty of skos:narrower.
     */
    public static final URI narrowerTransitive;

    /**
     * A notation, also known as classification code, is a string of characters such as "T58.5" or "303.4833" used to uniquely identify a concept within the scope of a given concept scheme.
     */
    public static final URI notation;

    /**
     * A general note, for any purpose.
     */
    public static final URI note;

    /**
     * The preferred lexical label for a resource, in a given language.
     */
    public static final URI prefLabel;

    /**
     * Relates a concept to a concept with which there is an associative semantic relationship.
     */
    public static final URI related;

    /**
     * skos:relatedMatch is used to state an associative mapping link between two conceptual resources in different concept schemes.
     */
    public static final URI relatedMatch;

    /**
     * A note that helps to clarify the meaning and/or the use of a concept.
     */
    public static final URI scopeNote;

    /**
     * Links a concept to a concept related by meaning.
     */
    public static final URI semanticRelation;

    /**
     * Relates a concept to the concept scheme that it is a top level concept of.
     */
    public static final URI topConceptOf;


    static{
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Collection = factory.createURI(SKOS.NAMESPACE, "Collection");
        Concept = factory.createURI(SKOS.NAMESPACE, "Concept");
        ConceptScheme = factory.createURI(SKOS.NAMESPACE, "ConceptScheme");
        OrderedCollection = factory.createURI(SKOS.NAMESPACE, "OrderedCollection");
        altLabel = factory.createURI(SKOS.NAMESPACE, "altLabel");
        broadMatch = factory.createURI(SKOS.NAMESPACE, "broadMatch");
        broader = factory.createURI(SKOS.NAMESPACE, "broader");
        broaderTransitive = factory.createURI(SKOS.NAMESPACE, "broaderTransitive");
        changeNote = factory.createURI(SKOS.NAMESPACE, "changeNote");
        closeMatch = factory.createURI(SKOS.NAMESPACE, "closeMatch");
        definition = factory.createURI(SKOS.NAMESPACE, "definition");
        editorialNote = factory.createURI(SKOS.NAMESPACE, "editorialNote");
        exactMatch = factory.createURI(SKOS.NAMESPACE, "exactMatch");
        example = factory.createURI(SKOS.NAMESPACE, "example");
        hasTopConcept = factory.createURI(SKOS.NAMESPACE, "hasTopConcept");
        hiddenLabel = factory.createURI(SKOS.NAMESPACE, "hiddenLabel");
        historyNote = factory.createURI(SKOS.NAMESPACE, "historyNote");
        inScheme = factory.createURI(SKOS.NAMESPACE, "inScheme");
        mappingRelation = factory.createURI(SKOS.NAMESPACE, "mappingRelation");
        member = factory.createURI(SKOS.NAMESPACE, "member");
        memberList = factory.createURI(SKOS.NAMESPACE, "memberList");
        narrowMatch = factory.createURI(SKOS.NAMESPACE, "narrowMatch");
        narrower = factory.createURI(SKOS.NAMESPACE, "narrower");
        narrowerTransitive = factory.createURI(SKOS.NAMESPACE, "narrowerTransitive");
        notation = factory.createURI(SKOS.NAMESPACE, "notation");
        note = factory.createURI(SKOS.NAMESPACE, "note");
        prefLabel = factory.createURI(SKOS.NAMESPACE, "prefLabel");
        related = factory.createURI(SKOS.NAMESPACE, "related");
        relatedMatch = factory.createURI(SKOS.NAMESPACE, "relatedMatch");
        scopeNote = factory.createURI(SKOS.NAMESPACE, "scopeNote");
        semanticRelation = factory.createURI(SKOS.NAMESPACE, "semanticRelation");
        topConceptOf = factory.createURI(SKOS.NAMESPACE, "topConceptOf");
    }
}
