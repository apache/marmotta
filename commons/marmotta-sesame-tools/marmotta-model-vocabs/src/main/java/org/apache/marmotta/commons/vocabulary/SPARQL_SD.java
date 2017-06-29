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

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Namespace SPARQL_SD - SparqlServiceDescription.
 * @see <a href="http://www.w3.org/TR/sparql11-service-description/">http://www.w3.org/TR/sparql11-service-description/</a>
 */
public class SPARQL_SD {

    public static final String NAMESPACE = "http://www.w3.org/ns/sparql-service-description#";

    public static final String PREFIX = "sd";

    /**
     * An instance of sd:Aggregate represents an aggregate that may be used in a
     * SPARQL aggregate query (for instance in a HAVING clause or SELECT
     * expression) besides the standard list of supported aggregates COUNT, SUM,
     * MIN, MAX, AVG, GROUP_CONCAT, and SAMPLE.
     */
    public static final IRI Aggregate;

    /**
     * sd:BasicFederatedQuery, when used as the object of the sd:feature
     * property, indicates that the SPARQL service supports basic federated
     * query using the SERVICE keyword as defined by SPARQL 1.1 Federation
     * Extensions.
     */
    public static final IRI BasicFederatedQuery;

    /**
     * An instance of sd:Dataset represents a RDF Dataset comprised of a default
     * graph and zero or more named graphs.
     */
    public static final IRI Dataset;

    /**
     * sd:DereferencesIRIs, when used as the object of the sd:feature property,
     * indicates that a SPARQL service will dereference IRIs used in FROM/FROM
     * NAMED and USING/USING NAMED clauses and use the resulting RDF in the
     * dataset during query evaluation.
     */
    public static final IRI DereferencesIRIs;

    /**
     * sd:EmptyGraphs, when used as the object of the sd:feature property,
     * indicates that the underlying graph store supports empty graphs. A graph
     * store that supports empty graphs MUST NOT remove graphs that are left
     * empty after triples are removed from them.
     */
    public static final IRI EmptyGraphs;

    /**
     * An instance of sd:EntailmentProfile represents a profile of an entailment
     * regime. An entailment profile MAY impose restrictions on what constitutes
     * valid RDF with respect to entailment.
     */
    public static final IRI EntailmentProfile;

    /**
     * An instance of sd:EntailmentRegime represents an entailment regime used
     * in basic graph pattern matching (as described by SPARQL 1.1 Query
     * Language).
     */
    public static final IRI EntailmentRegime;

    /**
     * An instance of sd:Feature represents a feature of a SPARQL service.
     * Specific types of features include functions, aggregates, languages, and
     * entailment regimes and profiles. This document defines five instances of
     * sd:Feature: sd:DereferencesIRIs, sd:UnionDefaultGraph,
     * sd:RequiresDataset, sd:EmptyGraphs, and sd:BasicFederatedQuery.
     */
    public static final IRI Feature;

    /**
     * An instance of sd:Function represents a function that may be used in a
     * SPARQL SELECT expression or a FILTER, HAVING, GROUP BY, ORDER BY, or BIND
     * clause.
     */
    public static final IRI Function;

    /**
     * An instance of sd:Graph represents the description of an RDF graph.
     */
    public static final IRI Graph;

    /**
     * An instance of sd:GraphCollection represents a collection of zero or more
     * named graph descriptions. Each named graph description belonging to an
     * sd:GraphCollection MUST be linked with the sd:namedGraph predicate.
     */
    public static final IRI GraphCollection;

    /**
     * An instance of sd:Language represents one of the SPARQL languages,
     * including specific configurations providing particular features or
     * extensions. This document defines three instances of sd:Language:
     * sd:SPARQL10Query, sd:SPARQL11Query, and sd:SPARQL11Update.
     */
    public static final IRI Language;

    /**
     * An instance of sd:NamedGraph represents a named graph having a name (via
     * sd:name) and an optional graph description (via sd:graph).
     */
    public static final IRI NamedGraph;

    /**
     * sd:RequiresDataset, when used as the object of the sd:feature property,
     * indicates that the SPARQL service requires an explicit dataset
     * declaration (based on either FROM/FROM NAMED clauses in a query,
     * USING/USING NAMED clauses in an update, or the appropriate SPARQL
     * Protocol parameters).
     */
    public static final IRI RequiresDataset;

    /**
     * sd:SPARQL10Query is an sd:Language representing the SPARQL 1.0 Query
     * language.
     */
    public static final IRI SPARQL10Query;

    /**
     * sd:SPARQL11Query is an sd:Language representing the SPARQL 1.1 Query
     * language.
     */
    public static final IRI SPARQL11Query;

    /**
     * sd:SPARQLUpdate is an sd:Language representing the SPARQL 1.1 Update
     * language.
     */
    public static final IRI SPARQL11Update;

    /**
     * An instance of sd:Service represents a SPARQL service made available via
     * the SPARQL Protocol.
     */
    public static final IRI Service;

    /**
     * sd:UnionDefaultGraph, when used as the object of the sd:feature property,
     * indicates that the default graph of the dataset used during query and
     * update evaluation (when an explicit dataset is not specified) is
     * comprised of the union of all the named graphs in that dataset.
     */
    public static final IRI UnionDefaultGraph;

    /**
     * Relates an instance of sd:Service to a description of the graphs which
     * are allowed in the construction of a dataset either via the SPARQL
     * Protocol, with FROM/FROM NAMED clauses in a query, or with USING/USING
     * NAMED in an update request, if the service limits the scope of dataset
     * construction.
     */
    public static final IRI availableGraphs;

    /**
     * Relates an instance of sd:Service to a description of the default dataset
     * available when no explicit dataset is specified in the query, update
     * request or via protocol parameters.
     */
    public static final IRI defaultDataset;

    /**
     * Relates an instance of sd:Service with a resource representing an
     * entailment regime used for basic graph pattern matching. This property is
     * intended for use when a single entailment regime by default applies to
     * all graphs in the default dataset of the service. In situations where a
     * different entailment regime applies to a specific graph in the dataset,
     * the sd:entailmentRegime property should be used to indicate this fact in
     * the description of that graph.
     */
    public static final IRI defaultEntailmentRegime;

    /**
     * Relates an instance of sd:Dataset to the description of its default
     * graph.
     */
    public static final IRI defaultGraph;

    /**
     * Relates an instance of sd:Service with a resource representing a
     * supported profile of the default entailment regime (as declared by
     * sd:defaultEntailmentRegime).
     */
    public static final IRI defaultSupportedEntailmentProfile;

    /**
     * The SPARQL endpoint of an sd:Service that implements the SPARQL Protocol
     * service. The object of the sd:endpoint property is an IRI.
     */
    public static final IRI endpoint;

    /**
     * Relates a named graph description with a resource representing an
     * entailment regime used for basic graph pattern matching over that graph.
     */
    public static final IRI entailmentRegime;

    /**
     * Relates an instance of sd:Service to an aggregate that may be used in a
     * SPARQL aggregate query (for instance in a HAVING clause or SELECT
     * expression) besides the standard list of supported aggregates COUNT, SUM,
     * MIN, MAX, AVG, GROUP_CONCAT, and SAMPLE
     */
    public static final IRI extensionAggregate;

    /**
     * Relates an instance of sd:Service to a function that may be used in a
     * SPARQL SELECT expression or a FILTER, HAVING, GROUP BY, ORDER BY, or BIND
     * clause.
     */
    public static final IRI extensionFunction;

    /**
     * Relates an instance of sd:Service with a resource representing a
     * supported feature.
     */
    public static final IRI feature;

    /**
     * Relates a named graph to its graph description.
     */
    public static final IRI graph;

    /**
     * Relates an instance of sd:Service to a format that is supported for
     * parsing RDF input; for example, via a SPARQL 1.1 Update LOAD statement,
     * or when IRIs are dereferenced in FROM/FROM NAMED/USING/USING NAMED
     * clauses.
     */
    public static final IRI inputFormat;

    /**
     * Relates an instance of sd:Service to a resource representing an
     * implemented extension to the SPARQL Query or Update language.
     */
    public static final IRI languageExtension;

    /**
     * Relates a named graph to the name by which it may be referenced in a
     * FROM/FROM NAMED clause. The object of the sd:name property is an IRI.
     */
    public static final IRI name;

    /**
     * Relates an instance of sd:GraphCollection (or its subclass sd:Dataset) to
     * the description of one of its named graphs. The description of such a
     * named graph MUST include the sd:name property and MAY include the
     * sd:graph property.
     */
    public static final IRI namedGraph;

    /**
     * Relates an instance of sd:Service to a resource representing an
     * implemented feature that extends the SPARQL Query or Update language and
     * that is accessed by using the named property.
     */
    public static final IRI propertyFeature;

    /**
     * Relates an instance of sd:Service to a format that is supported for
     * serializing query results.
     */
    public static final IRI resultFormat;

    /**
     * Relates a named graph description with a resource representing a
     * supported profile of the entailment regime (as declared by
     * sd:entailmentRegime) used for basic graph pattern matching over that
     * graph.
     */
    public static final IRI supportedEntailmentProfile;

    /**
     * Relates an instance of sd:Service to a SPARQL language (e.g. Query and
     * Update) that it implements.
     */
    public static final IRI supportedLanguage;

    static {
        ValueFactory factory = SimpleValueFactory.getInstance();
        Aggregate = factory.createIRI(SPARQL_SD.NAMESPACE, "Aggregate");
        BasicFederatedQuery = factory.createIRI(SPARQL_SD.NAMESPACE,
                "BasicFederatedQuery");
        Dataset = factory.createIRI(SPARQL_SD.NAMESPACE, "Dataset");
        DereferencesIRIs = factory.createIRI(SPARQL_SD.NAMESPACE,
                "DereferencesIRIs");
        EmptyGraphs = factory.createIRI(SPARQL_SD.NAMESPACE, "EmptyGraphs");
        EntailmentProfile = factory.createIRI(SPARQL_SD.NAMESPACE,
                "EntailmentProfile");
        EntailmentRegime = factory.createIRI(SPARQL_SD.NAMESPACE,
                "EntailmentRegime");
        Feature = factory.createIRI(SPARQL_SD.NAMESPACE, "Feature");
        Function = factory.createIRI(SPARQL_SD.NAMESPACE, "Function");
        Graph = factory.createIRI(SPARQL_SD.NAMESPACE, "Graph");
        GraphCollection = factory.createIRI(SPARQL_SD.NAMESPACE,
                "GraphCollection");
        Language = factory.createIRI(SPARQL_SD.NAMESPACE, "Language");
        NamedGraph = factory.createIRI(SPARQL_SD.NAMESPACE, "NamedGraph");
        RequiresDataset = factory.createIRI(SPARQL_SD.NAMESPACE,
                "RequiresDataset");
        SPARQL10Query = factory.createIRI(SPARQL_SD.NAMESPACE, "SPARQL10Query");
        SPARQL11Query = factory.createIRI(SPARQL_SD.NAMESPACE, "SPARQL11Query");
        SPARQL11Update = factory.createIRI(SPARQL_SD.NAMESPACE,
                "SPARQL11Update");
        Service = factory.createIRI(SPARQL_SD.NAMESPACE, "Service");
        UnionDefaultGraph = factory.createIRI(SPARQL_SD.NAMESPACE,
                "UnionDefaultGraph");
        availableGraphs = factory.createIRI(SPARQL_SD.NAMESPACE,
                "availableGraphs");
        defaultDataset = factory.createIRI(SPARQL_SD.NAMESPACE,
                "defaultDataset");
        defaultEntailmentRegime = factory.createIRI(SPARQL_SD.NAMESPACE,
                "defaultEntailmentRegime");
        defaultGraph = factory.createIRI(SPARQL_SD.NAMESPACE, "defaultGraph");
        defaultSupportedEntailmentProfile = factory.createIRI(
                SPARQL_SD.NAMESPACE, "defaultSupportedEntailmentProfile");
        endpoint = factory.createIRI(SPARQL_SD.NAMESPACE, "endpoint");
        entailmentRegime = factory.createIRI(SPARQL_SD.NAMESPACE,
                "entailmentRegime");
        extensionAggregate = factory.createIRI(SPARQL_SD.NAMESPACE,
                "extensionAggregate");
        extensionFunction = factory.createIRI(SPARQL_SD.NAMESPACE,
                "extensionFunction");
        feature = factory.createIRI(SPARQL_SD.NAMESPACE, "feature");
        graph = factory.createIRI(SPARQL_SD.NAMESPACE, "graph");
        inputFormat = factory.createIRI(SPARQL_SD.NAMESPACE, "inputFormat");
        languageExtension = factory.createIRI(SPARQL_SD.NAMESPACE,
                "languageExtension");
        name = factory.createIRI(SPARQL_SD.NAMESPACE, "name");
        namedGraph = factory.createIRI(SPARQL_SD.NAMESPACE, "namedGraph");
        propertyFeature = factory.createIRI(SPARQL_SD.NAMESPACE,
                "propertyFeature");
        resultFormat = factory.createIRI(SPARQL_SD.NAMESPACE, "resultFormat");
        supportedEntailmentProfile = factory.createIRI(SPARQL_SD.NAMESPACE,
                "supportedEntailmentProfile");
        supportedLanguage = factory.createIRI(SPARQL_SD.NAMESPACE,
                "supportedLanguage");
    }
}
