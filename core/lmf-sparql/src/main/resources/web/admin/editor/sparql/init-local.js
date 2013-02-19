/*
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
$(document).ready(function() {
	var sampleQuery1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\nSELECT ?title ?uri WHERE {\n\t?uri rdf:type <http://www.w3.org/ns/ma-ont#MediaResource>.\n\t?uri <http://www.w3.org/ns/ma-ont#title> ?title\n}\nLIMIT 10";

	var flintConfig = {
		"interface": {
			"toolbar": true,
			"menu": true
		},
		"namespaces": [
			{"name": "Friend of a friend", "prefix": "foaf", "uri": "http://xmlns.com/foaf/0.1/"},
			{"name": "XML schema", "prefix": "xsd", "uri": "http://www.w3.org/2001/XMLSchema#"},
			{"name": "SIOC", "prefix": "sioc", "uri": "http://rdfs.org/sioc/ns#"},
			{"name": "Resource Description Framework", "prefix": "rdf", "uri": "http://www.w3.org/1999/02/22-rdf-syntax-ns#"},
			{"name": "Resource Description Framework schema", "prefix": "rdfs", "uri": "http://www.w3.org/2000/01/rdf-schema#"},
			{"name": "Dublin Core", "prefix": "dc", "uri": "http://purl.org/dc/elements/1.1/"},
			{"name": "Dublin Core terms", "prefix": "dct", "uri": "http://purl.org/dc/terms/"},
			{"name": "Creative Commons", "prefix": "cc", "uri": "http://www.creativecommons.org/ns#"},
			{"name": "Web Ontology Language", "prefix": "owl", "uri": "http://www.w3.org/2002/07/owl#"},
			{"name": "Simple Knowledge Organisation System", "prefix": "skos", "uri": "http://www.w3.org/2004/02/skos/core#"},
			{"name": "Geography", "prefix": "geo", "uri": "http://www.w3.org/2003/01/geo/wgs84_pos#"},
			{"name": "Geonames", "prefix": "geonames", "uri": "http://www.geonames.org/ontology#"},
		        {"name": "DBPedia property", "prefix": "dbp", "uri": "http://dbpedia.org/property/"},
		        {"name": "Open Provenance Model Vocabulary", "prefix": "opmv", "uri": "http://purl.org./net/opmv/ns#"},
        	        {"name": "Functional Requirements for Bibliographic Records", "prefix": "frbr", "uri": "http://purl.org/vocab/frbr/core#"}

		],
		"defaultEndpointParameters": {
			"queryParameters": {
				"format": "output",
				"query": "query"
			},
			"selectFormats": [
				//{"name": "Plain text", "format": "text", "type": "text/plain"},
				{"name": "SPARQL-XML", "format": "sparql", "type": "application/sparql-results+xml"},
				{"name": "JSON", "format": "json", "type": "application/sparql-results+json"},
                {"name": "HTML", "format": "html", "type": "text/html"}
			],
			"constructFormats": [
				//{"name": "Plain text", "format": "text", "type": "text/plain"},
				{"name": "RDF/XML", "format": "rdfxml", "type": "application/rdf+xml"},
				{"name": "Turtle", "format": "turtle", "type": "application/turtle"}
			]
		},
		"endpoints": [	
			{"name": "Local LMF", "uri": _SERVER_URL+"sparql/select"},
			{"name": "LMF Showcase", "uri": "http://labs.newmedialab.at/RedBull/sparql/select", queries: [
				{"name": "Sample Query 1", "description": "Return all media resources.", "query": sampleQuery1}
			]}
        ]
	}
	var flintEd = new FlintEditor("flint-test", "sparql/images", flintConfig);
});