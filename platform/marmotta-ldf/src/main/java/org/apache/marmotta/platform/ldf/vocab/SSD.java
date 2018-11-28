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
 * Namespace Sparql-service-description.
 * Prefix: {@code <http://www.w3.org/ns/sparql-service-description#>}
 */
public class SSD {

    /** {@code http://www.w3.org/ns/sparql-service-description#} **/
    public static final String NAMESPACE = "http://www.w3.org/ns/sparql-service-description#";

    /** {@code sparql-service-description} **/
    public static final String PREFIX = "sparql-service-description";

    /**
     * Aggregate
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#Aggregate}.
     * <p>
     * An instance of sd:Aggregate represents an aggregate that may be used
	 * in a SPARQL aggregate query (for instance in a HAVING clause or SELECT
	 * expression) besides the standard list of supported aggregates COUNT,
	 * SUM, MIN, MAX, AVG, GROUP_CONCAT, and SAMPLE.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#Aggregate">Aggregate</a>
     */
    public static final URI Aggregate;

    /**
     * available graph descriptions
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#availableGraphs}.
     * <p>
     * Relates an instance of sd:Service to a description of the graphs which
	 * are allowed in the construction of a dataset either via the SPARQL
	 * Protocol, with FROM/FROM NAMED clauses in a query, or with USING/USING
	 * NAMED in an update request, if the service limits the scope of dataset
	 * construction.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#availableGraphs">availableGraphs</a>
     */
    public static final URI availableGraphs;

    /**
     * Basic Federated Query
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#BasicFederatedQuery}.
     * <p>
     * sd:BasicFederatedQuery, when used as the object of the sd:feature
	 * property, indicates that the SPARQL service supports basic federated
	 * query using the SERVICE keyword as defined by SPARQL 1.1 Federation
	 * Extensions.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#BasicFederatedQuery">BasicFederatedQuery</a>
     */
    public static final URI BasicFederatedQuery;

    /**
     * Dataset
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#Dataset}.
     * <p>
     * An instance of sd:Dataset represents a RDF Dataset comprised of a
	 * default graph and zero or more named graphs.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#Dataset">Dataset</a>
     */
    public static final URI Dataset;

    /**
     * default dataset description
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#defaultDataset}.
     * <p>
     * Relates an instance of sd:Service to a description of the default
	 * dataset available when no explicit dataset is specified in the query,
	 * update request or via protocol parameters.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#defaultDataset">defaultDataset</a>
     */
    public static final URI defaultDataset;

    /**
     * default entailment regime
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#defaultEntailmentRegime}.
     * <p>
     * Relates an instance of sd:Service with a resource representing an
	 * entailment regime used for basic graph pattern matching. This property
	 * is intended for use when a single entailment regime by default applies
	 * to all graphs in the default dataset of the service. In situations
	 * where a different entailment regime applies to a specific graph in the
	 * dataset, the sd:entailmentRegime property should be used to indicate
	 * this fact in the description of that graph.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#defaultEntailmentRegime">defaultEntailmentRegime</a>
     */
    public static final URI defaultEntailmentRegime;

    /**
     * default graph
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#defaultGraph}.
     * <p>
     * Relates an instance of sd:Dataset to the description of its default
	 * graph.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#defaultGraph">defaultGraph</a>
     */
    public static final URI defaultGraph;

    /**
     * default supported entailment profile
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#defaultSupportedEntailmentProfile}.
     * <p>
     * Relates an instance of sd:Service with a resource representing a
	 * supported profile of the default entailment regime (as declared by
	 * sd:defaultEntailmentRegime).
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#defaultSupportedEntailmentProfile">defaultSupportedEntailmentProfile</a>
     */
    public static final URI defaultSupportedEntailmentProfile;

    /**
     * Dereferences URIs
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#DereferencesURIs}.
     * <p>
     * sd:DereferencesURIs, when used as the object of the sd:feature
	 * property, indicates that a SPARQL service will dereference URIs used
	 * in FROM/FROM NAMED and USING/USING NAMED clauses and use the resulting
	 * RDF in the dataset during query evaluation.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#DereferencesURIs">DereferencesURIs</a>
     */
    public static final URI DereferencesURIs;

    /**
     * Empty Graphs
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#EmptyGraphs}.
     * <p>
     * sd:EmptyGraphs, when used as the object of the sd:feature property,
	 * indicates that the underlying graph store supports empty graphs. A
	 * graph store that supports empty graphs MUST NOT remove graphs that are
	 * left empty after triples are removed from them.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#EmptyGraphs">EmptyGraphs</a>
     */
    public static final URI EmptyGraphs;

    /**
     * endpoint
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#endpoint}.
     * <p>
     * The SPARQL endpoint of an sd:Service that implements the SPARQL
	 * Protocol service. The object of the sd:endpoint property is an IRI.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#endpoint">endpoint</a>
     */
    public static final URI endpoint;

    /**
     * Entailment Profile
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#EntailmentProfile}.
     * <p>
     * An instance of sd:EntailmentProfile represents a profile of an
	 * entailment regime. An entailment profile MAY impose restrictions on
	 * what constitutes valid RDF with respect to entailment.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#EntailmentProfile">EntailmentProfile</a>
     */
    public static final URI EntailmentProfile;

    /**
     * Entailment Regime
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#EntailmentRegime}.
     * <p>
     * An instance of sd:EntailmentRegime represents an entailment regime
	 * used in basic graph pattern matching (as described by SPARQL 1.1 Query
	 * Language).
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#EntailmentRegime">EntailmentRegime</a>
     */
    public static final URI EntailmentRegime;

    /**
     * entailment regime
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#entailmentRegime}.
     * <p>
     * Relates a named graph description with a resource representing an
	 * entailment regime used for basic graph pattern matching over that
	 * graph.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#entailmentRegime">entailmentRegime</a>
     */
    public static final URI entailmentRegime;

    /**
     * extension aggregate
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#extensionAggregate}.
     * <p>
     * Relates an instance of sd:Service to an aggregate that may be used in
	 * a SPARQL aggregate query (for instance in a HAVING clause or SELECT
	 * expression) besides the standard list of supported aggregates COUNT,
	 * SUM, MIN, MAX, AVG, GROUP_CONCAT, and SAMPLE
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#extensionAggregate">extensionAggregate</a>
     */
    public static final URI extensionAggregate;

    /**
     * extension function
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#extensionFunction}.
     * <p>
     * Relates an instance of sd:Service to a function that may be used in a
	 * SPARQL SELECT expression or a FILTER, HAVING, GROUP BY, ORDER BY, or
	 * BIND clause.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#extensionFunction">extensionFunction</a>
     */
    public static final URI extensionFunction;

    /**
     * Feature
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#Feature}.
     * <p>
     * An instance of sd:Feature represents a feature of a SPARQL service.
	 * Specific types of features include functions, aggregates, languages,
	 * and entailment regimes and profiles. This document defines five
	 * instances of sd:Feature: sd:DereferencesURIs, sd:UnionDefaultGraph,
	 * sd:RequiresDataset, sd:EmptyGraphs, and sd:BasicFederatedQuery.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#Feature">Feature</a>
     */
    public static final URI Feature;

    /**
     * feature
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#feature}.
     * <p>
     * Relates an instance of sd:Service with a resource representing a
	 * supported feature.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#feature">feature</a>
     */
    public static final URI feature;

    /**
     * Function
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#Function}.
     * <p>
     * An instance of sd:Function represents a function that may be used in a
	 * SPARQL SELECT expression or a FILTER, HAVING, GROUP BY, ORDER BY, or
	 * BIND clause.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#Function">Function</a>
     */
    public static final URI Function;

    /**
     * graph
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#graph}.
     * <p>
     * Relates a named graph to its graph description.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#graph">graph</a>
     */
    public static final URI graph;

    /**
     * Graph
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#Graph}.
     * <p>
     * An instance of sd:Graph represents the description of an RDF graph.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#Graph">Graph</a>
     */
    public static final URI Graph;

    /**
     * Graph Collection
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#GraphCollection}.
     * <p>
     * An instance of sd:GraphCollection represents a collection of zero or
	 * more named graph descriptions. Each named graph description belonging
	 * to an sd:GraphCollection MUST be linked with the sd:namedGraph
	 * predicate.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#GraphCollection">GraphCollection</a>
     */
    public static final URI GraphCollection;

    /**
     * input format
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#inputFormat}.
     * <p>
     * Relates an instance of sd:Service to a format that is supported for
	 * parsing RDF input; for example, via a SPARQL 1.1 Update LOAD
	 * statement, or when URIs are dereferenced in FROM/FROM
	 * NAMED/USING/USING NAMED clauses.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#inputFormat">inputFormat</a>
     */
    public static final URI inputFormat;

    /**
     * Language
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#Language}.
     * <p>
     * An instance of sd:Language represents one of the SPARQL languages,
	 * including specific configurations providing particular features or
	 * extensions. This document defines three instances of sd:Language:
	 * sd:SPARQL10Query, sd:SPARQL11Query, and sd:SPARQL11Update.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#Language">Language</a>
     */
    public static final URI Language;

    /**
     * language extension
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#languageExtension}.
     * <p>
     * Relates an instance of sd:Service to a resource representing an
	 * implemented extension to the SPARQL Query or Update language.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#languageExtension">languageExtension</a>
     */
    public static final URI languageExtension;

    /**
     * name
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#name}.
     * <p>
     * Relates a named graph to the name by which it may be referenced in a
	 * FROM/FROM NAMED clause. The object of the sd:name property is an IRI.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#name">name</a>
     */
    public static final URI name;

    /**
     * named graph
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#namedGraph}.
     * <p>
     * Relates an instance of sd:GraphCollection (or its subclass sd:Dataset)
	 * to the description of one of its named graphs. The description of such
	 * a named graph MUST include the sd:name property and MAY include the
	 * sd:graph property.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#namedGraph">namedGraph</a>
     */
    public static final URI namedGraph;

    /**
     * Named Graph
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#NamedGraph}.
     * <p>
     * An instance of sd:NamedGraph represents a named graph having a name
	 * (via sd:name) and an optional graph description (via sd:graph).
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#NamedGraph">NamedGraph</a>
     */
    public static final URI NamedGraph;

    /**
     * property feature
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#propertyFeature}.
     * <p>
     * Relates an instance of sd:Service to a resource representing an
	 * implemented feature that extends the SPARQL Query or Update language
	 * and that is accessed by using the named property.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#propertyFeature">propertyFeature</a>
     */
    public static final URI propertyFeature;

    /**
     * Requires Dataset
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#RequiresDataset}.
     * <p>
     * sd:RequiresDataset, when used as the object of the sd:feature
	 * property, indicates that the SPARQL service requires an explicit
	 * dataset declaration (based on either FROM/FROM NAMED clauses in a
	 * query, USING/USING NAMED clauses in an update, or the appropriate
	 * SPARQL Protocol parameters).
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#RequiresDataset">RequiresDataset</a>
     */
    public static final URI RequiresDataset;

    /**
     * result format
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#resultFormat}.
     * <p>
     * Relates an instance of sd:Service to a format that is supported for
	 * serializing query results.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#resultFormat">resultFormat</a>
     */
    public static final URI resultFormat;

    /**
     * Service
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#Service}.
     * <p>
     * An instance of sd:Service represents a SPARQL service made available
	 * via the SPARQL Protocol.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#Service">Service</a>
     */
    public static final URI Service;

    /**
     * SPARQL 1.0 Query
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#SPARQL10Query}.
     * <p>
     * sd:SPARQL10Query is an sd:Language representing the SPARQL 1.0 Query
	 * language.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#SPARQL10Query">SPARQL10Query</a>
     */
    public static final URI SPARQL10Query;

    /**
     * SPARQL 1.1 Query
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#SPARQL11Query}.
     * <p>
     * sd:SPARQL11Query is an sd:Language representing the SPARQL 1.1 Query
	 * language.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#SPARQL11Query">SPARQL11Query</a>
     */
    public static final URI SPARQL11Query;

    /**
     * SPARQL 1.1 Update
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#SPARQL11Update}.
     * <p>
     * sd:SPARQLUpdate is an sd:Language representing the SPARQL 1.1 Update
	 * language.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#SPARQL11Update">SPARQL11Update</a>
     */
    public static final URI SPARQL11Update;

    /**
     * supported entailment profile
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#supportedEntailmentProfile}.
     * <p>
     * Relates a named graph description with a resource representing a
	 * supported profile of the entailment regime (as declared by
	 * sd:entailmentRegime) used for basic graph pattern matching over that
	 * graph.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#supportedEntailmentProfile">supportedEntailmentProfile</a>
     */
    public static final URI supportedEntailmentProfile;

    /**
     * supported language
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#supportedLanguage}.
     * <p>
     * Relates an instance of sd:Service to a SPARQL language (e.g. Query and
	 * Update) that it implements.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#supportedLanguage">supportedLanguage</a>
     */
    public static final URI supportedLanguage;

    /**
     * Union Default Graph
     * <p>
     * {@code http://www.w3.org/ns/sparql-service-description#UnionDefaultGraph}.
     * <p>
     * sd:UnionDefaultGraph, when used as the object of the sd:feature
	 * property, indicates that the default graph of the dataset used during
	 * query and update evaluation (when an explicit dataset is not
	 * specified) is comprised of the union of all the named graphs in that
	 * dataset.
     *
     * @see <a href="http://www.w3.org/ns/sparql-service-description#UnionDefaultGraph">UnionDefaultGraph</a>
     */
    public static final URI UnionDefaultGraph;

    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();

        Aggregate = factory.createURI(SSD.NAMESPACE, "Aggregate");
        availableGraphs = factory.createURI(SSD.NAMESPACE, "availableGraphs");
        BasicFederatedQuery = factory.createURI(SSD.NAMESPACE, "BasicFederatedQuery");
        Dataset = factory.createURI(SSD.NAMESPACE, "Dataset");
        defaultDataset = factory.createURI(SSD.NAMESPACE, "defaultDataset");
        defaultEntailmentRegime = factory.createURI(SSD.NAMESPACE, "defaultEntailmentRegime");
        defaultGraph = factory.createURI(SSD.NAMESPACE, "defaultGraph");
        defaultSupportedEntailmentProfile = factory.createURI(SSD.NAMESPACE, "defaultSupportedEntailmentProfile");
        DereferencesURIs = factory.createURI(SSD.NAMESPACE, "DereferencesURIs");
        EmptyGraphs = factory.createURI(SSD.NAMESPACE, "EmptyGraphs");
        endpoint = factory.createURI(SSD.NAMESPACE, "endpoint");
        EntailmentProfile = factory.createURI(SSD.NAMESPACE, "EntailmentProfile");
        EntailmentRegime = factory.createURI(SSD.NAMESPACE, "EntailmentRegime");
        entailmentRegime = factory.createURI(SSD.NAMESPACE, "entailmentRegime");
        extensionAggregate = factory.createURI(SSD.NAMESPACE, "extensionAggregate");
        extensionFunction = factory.createURI(SSD.NAMESPACE, "extensionFunction");
        Feature = factory.createURI(SSD.NAMESPACE, "Feature");
        feature = factory.createURI(SSD.NAMESPACE, "feature");
        Function = factory.createURI(SSD.NAMESPACE, "Function");
        graph = factory.createURI(SSD.NAMESPACE, "graph");
        Graph = factory.createURI(SSD.NAMESPACE, "Graph");
        GraphCollection = factory.createURI(SSD.NAMESPACE, "GraphCollection");
        inputFormat = factory.createURI(SSD.NAMESPACE, "inputFormat");
        Language = factory.createURI(SSD.NAMESPACE, "Language");
        languageExtension = factory.createURI(SSD.NAMESPACE, "languageExtension");
        name = factory.createURI(SSD.NAMESPACE, "name");
        namedGraph = factory.createURI(SSD.NAMESPACE, "namedGraph");
        NamedGraph = factory.createURI(SSD.NAMESPACE, "NamedGraph");
        propertyFeature = factory.createURI(SSD.NAMESPACE, "propertyFeature");
        RequiresDataset = factory.createURI(SSD.NAMESPACE, "RequiresDataset");
        resultFormat = factory.createURI(SSD.NAMESPACE, "resultFormat");
        Service = factory.createURI(SSD.NAMESPACE, "Service");
        SPARQL10Query = factory.createURI(SSD.NAMESPACE, "SPARQL10Query");
        SPARQL11Query = factory.createURI(SSD.NAMESPACE, "SPARQL11Query");
        SPARQL11Update = factory.createURI(SSD.NAMESPACE, "SPARQL11Update");
        supportedEntailmentProfile = factory.createURI(SSD.NAMESPACE, "supportedEntailmentProfile");
        supportedLanguage = factory.createURI(SSD.NAMESPACE, "supportedLanguage");
        UnionDefaultGraph = factory.createURI(SSD.NAMESPACE, "UnionDefaultGraph");
    }

    private SSD() {
        //static access only
    }

}
