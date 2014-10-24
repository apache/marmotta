/*
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
package org.apache.marmotta.platform.ldf.vocab;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Namespace Void.
 * Prefix: {@code <http://rdfs.org/ns/void#>}
 */
public class VOID {

    /** {@code http://rdfs.org/ns/void#} **/
    public static final String NAMESPACE = "http://rdfs.org/ns/void#";

    /** {@code void} **/
    public static final String PREFIX = "void";

    /**
     * class
     * <p>
     * {@code http://rdfs.org/ns/void#class}.
     * <p>
     * The rdfs:Class that is the rdf:type of all entities in a class-based
     * partition.
     *
     * @see <a href="http://rdfs.org/ns/void#class">class</a>
     */
    //public static final URI class;

    /**
     * classes
     * <p>
     * {@code http://rdfs.org/ns/void#classes}.
     * <p>
     * The total number of distinct classes in a void:Dataset. In other
     * words, the number of distinct resources occuring as objects of
     * rdf:type triples in the dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#classes">classes</a>
     */
    public static final URI classes;

    /**
     * class partition
     * <p>
     * {@code http://rdfs.org/ns/void#classPartition}.
     * <p>
     * A subset of a void:Dataset that contains only the entities of a
     * certain rdfs:Class.
     *
     * @see <a href="http://rdfs.org/ns/void#classPartition">classPartition</a>
     */
    public static final URI classPartition;

    /**
     * Data Dump
     * <p>
     * {@code http://rdfs.org/ns/void#dataDump}.
     * <p>
     * An RDF dump, partial or complete, of a void:Dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#dataDump">dataDump</a>
     */
    public static final URI dataDump;

    /**
     * dataset
     * <p>
     * {@code http://rdfs.org/ns/void#Dataset}.
     * <p>
     * A set of RDF triples that are published, maintained or aggregated by a
     * single provider.
     *
     * @see <a href="http://rdfs.org/ns/void#Dataset">Dataset</a>
     */
    public static final URI Dataset;

    /**
     * dataset description
     * <p>
     * {@code http://rdfs.org/ns/void#DatasetDescription}.
     * <p>
     * A web resource whose foaf:primaryTopic or foaf:topics include
     * void:Datasets.
     *
     * @see <a href="http://rdfs.org/ns/void#DatasetDescription">DatasetDescription</a>
     */
    public static final URI DatasetDescription;

    /**
     * distinct objects
     * <p>
     * {@code http://rdfs.org/ns/void#distinctObjects}.
     * <p>
     * The total number of distinct objects in a void:Dataset. In other
     * words, the number of distinct resources that occur in the object
     * position of triples in the dataset. Literals are included in this
     * count.
     *
     * @see <a href="http://rdfs.org/ns/void#distinctObjects">distinctObjects</a>
     */
    public static final URI distinctObjects;

    /**
     * distinct subjects
     * <p>
     * {@code http://rdfs.org/ns/void#distinctSubjects}.
     * <p>
     * The total number of distinct subjects in a void:Dataset. In other
     * words, the number of distinct resources that occur in the subject
     * position of triples in the dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#distinctSubjects">distinctSubjects</a>
     */
    public static final URI distinctSubjects;

    /**
     * number of documents
     * <p>
     * {@code http://rdfs.org/ns/void#documents}.
     * <p>
     * The total number of documents, for datasets that are published as a
     * set of individual documents, such as RDF/XML documents or
     * RDFa-annotated web pages. Non-RDF documents, such as web pages in HTML
     * or images, are usually not included in this count. This property is
     * intended for datasets where the total number of triples or entities is
     * hard to determine. void:triples or void:entities should be preferred
     * where practical.
     *
     * @see <a href="http://rdfs.org/ns/void#documents">documents</a>
     */
    public static final URI documents;

    /**
     * number of entities
     * <p>
     * {@code http://rdfs.org/ns/void#entities}.
     * <p>
     * The total number of entities that are described in a void:Dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#entities">entities</a>
     */
    public static final URI entities;

    /**
     * example resource of dataset
     * <p>
     * {@code http://rdfs.org/ns/void#exampleResource}.
     *
     * @see <a href="http://rdfs.org/ns/void#exampleResource">exampleResource</a>
     */
    public static final URI exampleResource;

    /**
     * feature
     * <p>
     * {@code http://rdfs.org/ns/void#feature}.
     *
     * @see <a href="http://rdfs.org/ns/void#feature">feature</a>
     */
    public static final URI feature;

    /**
     * in dataset
     * <p>
     * {@code http://rdfs.org/ns/void#inDataset}.
     * <p>
     * Points to the void:Dataset that a document is a part of.
     *
     * @see <a href="http://rdfs.org/ns/void#inDataset">inDataset</a>
     */
    public static final URI inDataset;

    /**
     * a link predicate
     * <p>
     * {@code http://rdfs.org/ns/void#linkPredicate}.
     *
     * @see <a href="http://rdfs.org/ns/void#linkPredicate">linkPredicate</a>
     */
    public static final URI linkPredicate;

    /**
     * linkset
     * <p>
     * {@code http://rdfs.org/ns/void#Linkset}.
     * <p>
     * A collection of RDF links between two void:Datasets.
     *
     * @see <a href="http://rdfs.org/ns/void#Linkset">Linkset</a>
     */
    public static final URI Linkset;

    /**
     * Objects Target
     * <p>
     * {@code http://rdfs.org/ns/void#objectsTarget}.
     * <p>
     * The dataset describing the objects of the triples contained in the
     * Linkset.
     *
     * @see <a href="http://rdfs.org/ns/void#objectsTarget">objectsTarget</a>
     */
    public static final URI objectsTarget;

    /**
     * open search description
     * <p>
     * {@code http://rdfs.org/ns/void#openSearchDescription}.
     * <p>
     * An OpenSearch description document for a free-text search service over
     * a void:Dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#openSearchDescription">openSearchDescription</a>
     */
    public static final URI openSearchDescription;

    /**
     * number of properties
     * <p>
     * {@code http://rdfs.org/ns/void#properties}.
     * <p>
     * The total number of distinct properties in a void:Dataset. In other
     * words, the number of distinct resources that occur in the predicate
     * position of triples in the dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#properties">properties</a>
     */
    public static final URI properties;

    /**
     * property
     * <p>
     * {@code http://rdfs.org/ns/void#property}.
     * <p>
     * The rdf:Property that is the predicate of all triples in a
     * property-based partition.
     *
     * @see <a href="http://rdfs.org/ns/void#property">property</a>
     */
    public static final URI property;

    /**
     * property partition
     * <p>
     * {@code http://rdfs.org/ns/void#propertyPartition}.
     * <p>
     * A subset of a void:Dataset that contains only the triples of a certain
     * rdf:Property.
     *
     * @see <a href="http://rdfs.org/ns/void#propertyPartition">propertyPartition</a>
     */
    public static final URI propertyPartition;

    /**
     * root resource
     * <p>
     * {@code http://rdfs.org/ns/void#rootResource}.
     * <p>
     * A top concept or entry point for a void:Dataset that is structured in
     * a tree-like fashion. All resources in a dataset can be reached by
     * following links from its root resources in a small number of steps.
     *
     * @see <a href="http://rdfs.org/ns/void#rootResource">rootResource</a>
     */
    public static final URI rootResource;

    /**
     * has a SPARQL endpoint at
     * <p>
     * {@code http://rdfs.org/ns/void#sparqlEndpoint}.
     *
     * @see <a href="http://rdfs.org/ns/void#sparqlEndpoint">sparqlEndpoint</a>
     */
    public static final URI sparqlEndpoint;

    /**
     * Subjects Target
     * <p>
     * {@code http://rdfs.org/ns/void#subjectsTarget}.
     * <p>
     * The dataset describing the subjects of triples contained in the
     * Linkset.
     *
     * @see <a href="http://rdfs.org/ns/void#subjectsTarget">subjectsTarget</a>
     */
    public static final URI subjectsTarget;

    /**
     * has subset
     * <p>
     * {@code http://rdfs.org/ns/void#subset}.
     *
     * @see <a href="http://rdfs.org/ns/void#subset">subset</a>
     */
    public static final URI subset;

    /**
     * Target
     * <p>
     * {@code http://rdfs.org/ns/void#target}.
     * <p>
     * One of the two datasets linked by the Linkset.
     *
     * @see <a href="http://rdfs.org/ns/void#target">target</a>
     */
    public static final URI target;

    /**
     * technical feature
     * <p>
     * {@code http://rdfs.org/ns/void#TechnicalFeature}.
     * <p>
     * A technical feature of a void:Dataset, such as a supported RDF
     * serialization format.
     *
     * @see <a href="http://rdfs.org/ns/void#TechnicalFeature">TechnicalFeature</a>
     */
    public static final URI TechnicalFeature;

    /**
     * number of triples
     * <p>
     * {@code http://rdfs.org/ns/void#triples}.
     * <p>
     * The total number of triples contained in a void:Dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#triples">triples</a>
     */
    public static final URI triples;

    /**
     * has an URI look-up endpoint at
     * <p>
     * {@code http://rdfs.org/ns/void#uriLookupEndpoint}.
     * <p>
     * Defines a simple URI look-up protocol for accessing a dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#uriLookupEndpoint">uriLookupEndpoint</a>
     */
    public static final URI uriLookupEndpoint;

    /**
     * has URI regular expression pattern
     * <p>
     * {@code http://rdfs.org/ns/void#uriRegexPattern}.
     * <p>
     * Defines a regular expression pattern matching URIs in the dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#uriRegexPattern">uriRegexPattern</a>
     */
    public static final URI uriRegexPattern;

    /**
     * URI space
     * <p>
     * {@code http://rdfs.org/ns/void#uriSpace}.
     * <p>
     * A URI that is a common string prefix of all the entity URIs in a
     * void:Dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#uriSpace">uriSpace</a>
     */
    public static final URI uriSpace;

    /**
     * vocabulary
     * <p>
     * {@code http://rdfs.org/ns/void#vocabulary}.
     * <p>
     * A vocabulary that is used in the dataset.
     *
     * @see <a href="http://rdfs.org/ns/void#vocabulary">vocabulary</a>
     */
    public static final URI vocabulary;

    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();

        //class = factory.createURI(VOID.NAMESPACE, "class");
        classes = factory.createURI(VOID.NAMESPACE, "classes");
        classPartition = factory.createURI(VOID.NAMESPACE, "classPartition");
        dataDump = factory.createURI(VOID.NAMESPACE, "dataDump");
        Dataset = factory.createURI(VOID.NAMESPACE, "Dataset");
        DatasetDescription = factory.createURI(VOID.NAMESPACE, "DatasetDescription");
        distinctObjects = factory.createURI(VOID.NAMESPACE, "distinctObjects");
        distinctSubjects = factory.createURI(VOID.NAMESPACE, "distinctSubjects");
        documents = factory.createURI(VOID.NAMESPACE, "documents");
        entities = factory.createURI(VOID.NAMESPACE, "entities");
        exampleResource = factory.createURI(VOID.NAMESPACE, "exampleResource");
        feature = factory.createURI(VOID.NAMESPACE, "feature");
        inDataset = factory.createURI(VOID.NAMESPACE, "inDataset");
        linkPredicate = factory.createURI(VOID.NAMESPACE, "linkPredicate");
        Linkset = factory.createURI(VOID.NAMESPACE, "Linkset");
        objectsTarget = factory.createURI(VOID.NAMESPACE, "objectsTarget");
        openSearchDescription = factory.createURI(VOID.NAMESPACE, "openSearchDescription");
        properties = factory.createURI(VOID.NAMESPACE, "properties");
        property = factory.createURI(VOID.NAMESPACE, "property");
        propertyPartition = factory.createURI(VOID.NAMESPACE, "propertyPartition");
        rootResource = factory.createURI(VOID.NAMESPACE, "rootResource");
        sparqlEndpoint = factory.createURI(VOID.NAMESPACE, "sparqlEndpoint");
        subjectsTarget = factory.createURI(VOID.NAMESPACE, "subjectsTarget");
        subset = factory.createURI(VOID.NAMESPACE, "subset");
        target = factory.createURI(VOID.NAMESPACE, "target");
        TechnicalFeature = factory.createURI(VOID.NAMESPACE, "TechnicalFeature");
        triples = factory.createURI(VOID.NAMESPACE, "triples");
        uriLookupEndpoint = factory.createURI(VOID.NAMESPACE, "uriLookupEndpoint");
        uriRegexPattern = factory.createURI(VOID.NAMESPACE, "uriRegexPattern");
        uriSpace = factory.createURI(VOID.NAMESPACE, "uriSpace");
        vocabulary = factory.createURI(VOID.NAMESPACE, "vocabulary");
    }

    private VOID() {
        //static access only
    }

}
