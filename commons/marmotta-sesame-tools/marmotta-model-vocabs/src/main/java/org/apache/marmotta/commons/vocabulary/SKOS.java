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
 * Namespace SKOS
 */
public class SKOS {

    public static final String NAMESPACE = "http://www.w3.org/2004/02/skos/core#";

    public static final String PREFIX = "skos";

    /**
     * A meaningful collection of concepts.
     */
    public static final IRI Collection;

    /**
     * An idea or notion; a unit of thought.
     */
    public static final IRI Concept;

    /**
     * A set of concepts, optionally including statements about semantic relationships between those concepts.
     */
    public static final IRI ConceptScheme;

    /**
     * An ordered collection of concepts, where both the grouping and the ordering are meaningful.
     */
    public static final IRI OrderedCollection;

    /**
     * An alternative lexical label for a resource.
     */
    public static final IRI altLabel;

    /**
     * skos:broadMatch is used to state a hierarchical mapping link between two conceptual resources in different concept schemes.
     */
    public static final IRI broadMatch;

    /**
     * Relates a concept to a concept that is more general in meaning.
     */
    public static final IRI broader;

    /**
     * skos:broaderTransitive is a transitive superproperty of skos:broader.
     */
    public static final IRI broaderTransitive;

    /**
     * A note about a modification to a concept.
     */
    public static final IRI changeNote;

    /**
     * skos:closeMatch is used to link two concepts that are sufficiently similar that they can be used interchangeably in some information retrieval applications. In order to avoid the possibility of "compound errors" when combining mappings across more than two concept schemes, skos:closeMatch is not declared to be a transitive property.
     */
    public static final IRI closeMatch;

    /**
     * A statement or formal explanation of the meaning of a concept.
     */
    public static final IRI definition;

    /**
     * A note for an editor, translator or maintainer of the vocabulary.
     */
    public static final IRI editorialNote;

    /**
     * skos:exactMatch is used to link two concepts, indicating a high degree of confidence that the concepts can be used interchangeably across a wide range of information retrieval applications. skos:exactMatch is a transitive property, and is a sub-property of skos:closeMatch.
     */
    public static final IRI exactMatch;

    /**
     * An example of the use of a concept.
     */
    public static final IRI example;

    /**
     * Relates, by convention, a concept scheme to a concept which is topmost in the broader/narrower concept hierarchies for that scheme, providing an entry point to these hierarchies.
     */
    public static final IRI hasTopConcept;

    /**
     * A lexical label for a resource that should be hidden when generating visual displays of the resource, but should still be accessible to free text search operations.
     */
    public static final IRI hiddenLabel;

    /**
     * A note about the past state/use/meaning of a concept.
     */
    public static final IRI historyNote;

    /**
     * Relates a resource (for example a concept) to a concept scheme in which it is included.
     */
    public static final IRI inScheme;

    /**
     * Relates two concepts coming, by convention, from different schemes, and that have comparable meanings
     */
    public static final IRI mappingRelation;

    /**
     * Relates a collection to one of its members.
     */
    public static final IRI member;

    /**
     * Relates an ordered collection to the RDF list containing its members.
     */
    public static final IRI memberList;

    /**
     * skos:narrowMatch is used to state a hierarchical mapping link between two conceptual resources in different concept schemes.
     */
    public static final IRI narrowMatch;

    /**
     * Relates a concept to a concept that is more specific in meaning.
     */
    public static final IRI narrower;

    /**
     * skos:narrowerTransitive is a transitive superproperty of skos:narrower.
     */
    public static final IRI narrowerTransitive;

    /**
     * A notation, also known as classification code, is a string of characters such as "T58.5" or "303.4833" used to uniquely identify a concept within the scope of a given concept scheme.
     */
    public static final IRI notation;

    /**
     * A general note, for any purpose.
     */
    public static final IRI note;

    /**
     * The preferred lexical label for a resource, in a given language.
     */
    public static final IRI prefLabel;

    /**
     * Relates a concept to a concept with which there is an associative semantic relationship.
     */
    public static final IRI related;

    /**
     * skos:relatedMatch is used to state an associative mapping link between two conceptual resources in different concept schemes.
     */
    public static final IRI relatedMatch;

    /**
     * A note that helps to clarify the meaning and/or the use of a concept.
     */
    public static final IRI scopeNote;

    /**
     * Links a concept to a concept related by meaning.
     */
    public static final IRI semanticRelation;

    /**
     * Relates a concept to the concept scheme that it is a top level concept of.
     */
    public static final IRI topConceptOf;


    static{
        ValueFactory factory = SimpleValueFactory.getInstance();
        Collection = factory.createIRI(SKOS.NAMESPACE, "Collection");
        Concept = factory.createIRI(SKOS.NAMESPACE, "Concept");
        ConceptScheme = factory.createIRI(SKOS.NAMESPACE, "ConceptScheme");
        OrderedCollection = factory.createIRI(SKOS.NAMESPACE, "OrderedCollection");
        altLabel = factory.createIRI(SKOS.NAMESPACE, "altLabel");
        broadMatch = factory.createIRI(SKOS.NAMESPACE, "broadMatch");
        broader = factory.createIRI(SKOS.NAMESPACE, "broader");
        broaderTransitive = factory.createIRI(SKOS.NAMESPACE, "broaderTransitive");
        changeNote = factory.createIRI(SKOS.NAMESPACE, "changeNote");
        closeMatch = factory.createIRI(SKOS.NAMESPACE, "closeMatch");
        definition = factory.createIRI(SKOS.NAMESPACE, "definition");
        editorialNote = factory.createIRI(SKOS.NAMESPACE, "editorialNote");
        exactMatch = factory.createIRI(SKOS.NAMESPACE, "exactMatch");
        example = factory.createIRI(SKOS.NAMESPACE, "example");
        hasTopConcept = factory.createIRI(SKOS.NAMESPACE, "hasTopConcept");
        hiddenLabel = factory.createIRI(SKOS.NAMESPACE, "hiddenLabel");
        historyNote = factory.createIRI(SKOS.NAMESPACE, "historyNote");
        inScheme = factory.createIRI(SKOS.NAMESPACE, "inScheme");
        mappingRelation = factory.createIRI(SKOS.NAMESPACE, "mappingRelation");
        member = factory.createIRI(SKOS.NAMESPACE, "member");
        memberList = factory.createIRI(SKOS.NAMESPACE, "memberList");
        narrowMatch = factory.createIRI(SKOS.NAMESPACE, "narrowMatch");
        narrower = factory.createIRI(SKOS.NAMESPACE, "narrower");
        narrowerTransitive = factory.createIRI(SKOS.NAMESPACE, "narrowerTransitive");
        notation = factory.createIRI(SKOS.NAMESPACE, "notation");
        note = factory.createIRI(SKOS.NAMESPACE, "note");
        prefLabel = factory.createIRI(SKOS.NAMESPACE, "prefLabel");
        related = factory.createIRI(SKOS.NAMESPACE, "related");
        relatedMatch = factory.createIRI(SKOS.NAMESPACE, "relatedMatch");
        scopeNote = factory.createIRI(SKOS.NAMESPACE, "scopeNote");
        semanticRelation = factory.createIRI(SKOS.NAMESPACE, "semanticRelation");
        topConceptOf = factory.createIRI(SKOS.NAMESPACE, "topConceptOf");
    }
}
