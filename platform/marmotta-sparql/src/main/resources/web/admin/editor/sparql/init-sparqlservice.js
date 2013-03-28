/*
 * Copyright (c) 2011 TSO Ltd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal 
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is furnished 
 * to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS 
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
$(document).ready(function(){
		
	var endpoint = document.URL;	
	
	addStylesheet("http://openuplabs.tso.co.uk/script/sparql/lib/codemirror.css");		
	addStylesheet("http://openuplabs.tso.co.uk/script/sparql/css/sparqlcolors.css");
	addStylesheet("http://openuplabs.tso.co.uk/script/sparql/css/docs.css");
	jQuery.getScript("http://openuplabs.tso.co.uk/script/sparql/lib/codemirror.js", function() {
		jQuery.getScript("http://openuplabs.tso.co.uk/script/sparql/sparqlmode_ll1.js", function () {
			jQuery.getScript("http://openuplabs.tso.co.uk/script/sparql/flint-editor.js", function () {
				init(endpoint);
			});
		});
	});
	
});

function init(endpoint) {
	
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
				{"name": "Plain text", "format": "text", "type": "text/plain"},
				{"name": "SPARQL-XML", "format": "sparql", "type": "application/sparql-results+xml"},
				{"name": "JSON", "format": "json", "type": "application/sparql-results+json"}
			],
			"constructFormats": [
				{"name": "Plain text", "format": "text", "type": "text/plain"},
				{"name": "RDF/XML", "format": "rdfxml", "type": "application/rdf+xml"},
				{"name": "Turtle", "format": "turtle", "type": "application/turtle"}
			],			
		},
		"endpoints": [	
			{"name": document.title,
				"uri": endpoint,				
			}
		]
	}
	
	// add Flint button
	$('#description').after('<a href="" id="flint-button">Query endpoint with Flint SPARQL Editor</a>');	
	
	$('#flint-button').click(function(e) {
		e.preventDefault();
		$('#flint-button').after('<div style="display:none; width: 950px" id="flint-test"></div>');
		$('#flint-button').remove();
		var flintEd = new FlintEditor("flint-test","http://openuplabs.tso.co.uk/script/sparql/images", flintConfig);
		$('#flint-test').show();
	});
	
}

function addStylesheet(url) {
	var css = document.createElement( 'link' );
	css.href = url;
	css.rel= "stylesheet";
	$('head').append( css );
}
