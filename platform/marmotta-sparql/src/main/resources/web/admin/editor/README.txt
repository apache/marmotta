==============================
Flint SPARQL Editor v0.5
==============================

Released 23 June 2011

http://openuplabs.tso.co.uk/demos/sparqleditor

Bug reports/discussion at 

https://groups.google.com/group/flint-sparql-editor

Created by TSO - www.tso.co.uk

You can contact the developers on the Flint Google Group or at flintsparqleditor@tso.co.uk.  If you are deploying Flint on your site, please let us know and we'll keep you informed of new releases.


Installation instructions
---------------------------------

Flint can be used locally or on a website.

To install Flint simply unzip all of the files in the download file into a folder, maintaining the structure of the download ZIP.

If the installation is local you can 'run' Flint by opening sparqleditor.html in your browser.

To use within a web page you need to reference the necessary files. This can be done by including the following lines in your web page:

<script type="text/javascript" src="sparql/jquery-1.5.2.min.js">//</script>
<script type="text/javascript" src="sparql/lib/codemirror.js">//</script>
<script type="text/javascript" src="sparql/sparqlmode_ll1.js">//</script>
<script type="text/javascript" src="sparql/flint-editor.js">//</script>
<link rel="stylesheet" href="sparql/lib/codemirror.css"/>
<link rel="stylesheet" href="sparql/css/sparqlcolors.css"/>
<link rel="stylesheet" href="sparql/css/docs.css"/>

To create an instance of Flint in a web page you will need to use Javascript to create a FlintEditor object, passing in the relevant parameters. An example if given below:

var flintEd = new FlintEditor("flint-test", "sparql/images", flintConfig);

The parameters are: 

The id of a container element (e.g. a <div> element) in which Flint will be created
The relative path of the images for Flint. By default this will use the supplied images
A Flint configuration object. You can simply look at init-local.js as an example of how to do it or follow the instructions below.


Flint Configuration
--------------------------

Flint is configured using a JSON array. An example file is given below:

	var flintConfig = {
		"interface": {
			"toolbar": true,
			"menu": true
		},
		"namespaces": [
			{"name": "Friend of a friend", "prefix": "foaf", "uri": "http://xmlns.com/foaf/0.1/"},
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
			]
		},
		"endpoints": [	
			{"name": "Legislation",
				"uri": "http://gov.tso.co.uk/legislation/sparql",
				queries: [
					{"name": "Sample Legislation Query 1", "description": "Select up to 100 pieces of legislation after a given date, with most recent first.", "query": sampleQuery1},
					{"name": "Sample Legislation Query 2", "description": "The RDF description of each piece of legislation is stored in a separate named graph, so all of the RDF for the item can be retrieved with the following query.", "query": sampleQuery2}
				]
			}
		}

interface:
This defines whether certain aspects of the UI should be visible or not. The options are:

toolbar
menu

The permissible values are true or false

namespaces:
This defines the namespaces that the application 'knows' about. This should be an array of namespaces. Each namespace should define the following:

	name					UI name
	prefix					A prefix for the application to use for the namespace
	uri						The URI of the namespace

defaultEndpointParameters:
These are default values to use for endpoints. In future versions these will be customisable at the endpoint level

queryParameters
Details about the query parameter settings. This should define:

	format					The query parameter name for the format to return. Note this is not currently used
	query					They query parameter that will carry the SPARQL
	
selectFormats
This should be an array of possible formats that can be returned for SELECT queries. Each format should define:

	name					UI name
	format					query parameter value. Note this is not currently used
	type						The MIME type of the format. This is used in the request and assumes that an endpoint correctly performs content negotiation

constructFormats
This should be an array of possible formats that can be returned for CONSTRUCT queries. Each format should define:

	name					UI name
	format					query parameter value. Note this is not currently used
	type						The MIME type of the format. This is used in the request and assumes that an endpoint correctly performs content negotiation

endpoints
This defines a list of pre-defined endpoints that will appear in the UI. Each endpoint should define:

	name					UI name
	uri						The URI of the endpoint
	queries					An array of sample queries for this endpoint (optional). Each of these queries should define:
	
		name				UI name of query
		description		A description of the what the query does
		query				The actual query. (Note you will need to do some tricky escaping, which is why in init-local.js they are defined using variables)
