LDPath Linked Data Query CLI
============================

LDPath is a query language designed for querying the Linked Data Cloud. It
gives you the possibility to navigate between interlinked resources by
following path expressions.

This module provides a simple command-line application for querying the
Linked Data Cloud with LDPath (using LDClient and LDCache). It will
transparently fetch resources and triples as needed as long as they conform
to the Linked Data principles or are supported by one of our LDClient
providers (currently Facebook, YouTube, Vimeo, Wikipedia).

To get an overview over the available command line options, run:

java -jar ldquery-${project.version}.jar


1. Basic Usage
--------------

For the basic usage, you need to provide the URI of a Linked Data resource
(e.g. http://dbpedia.org/resource/Salzburg) and an LDPath expression (e.g.
rdfs:label). The following query would give you the Spanish label for
Salzburg:

java -jar ldquery-${project.version}.jar \
     -context http://dbpedia.org/resource/Salzburg \
     -path "rdfs:label[@es]"

LDPath allows for very complex path expression. The full language documen-
tation is available at:

http://marmotta.apache.org/ldpath/language.html


2. Path Programs
----------------

In addition to simple path expressions, it is also possible to run complete
path programs as queries against resources. A path program consists of
several field definitions, e.g.

@prefix dbp: <http://dbpedia.org/ontology/>
title       = rdfs:label[@en];
description = dbp:abstract[@en];
country     = dbp:country / rdfs:label[@en];
type        = rdf:type / rdfs:label[@en];

will retrieve the label, short description, name of the country and names
of the types associated with the resource. LDPath programs need to be
stored in a file and can be executed using:

java -jar ldquery-${project.version}.jar \
     -context http://dbpedia.org/resource/Salzburg \
     -program <filename>



3. Querying Non-Linked Data Resources
-------------------------------------

The Apache Marmotta LDClient library also supports some common non-linked
data resources by mapping their proprietary data structures to RDF, among
them Facebook, YouTube, Vimeo and Wikipedia. For example, the label of
Salzburg can also be retrieved from Wikipedia:

java -jar ldquery-${project.version}.jar \
     -context http://en.wikipedia.org/wiki/Salzburg \
     -path "dct:title"

... or from Facebook:

java -jar ldquery-${project.version}.jar \
     -context http://graph.facebook.com/102189213155511 \
     -path "dct:title"

A full list of supported providers with usage instructions is given on:

http://marmotta.apache.org/ldclient/modules.html



4. Local Caching
----------------

The Linked Data Backend also bundles Apache Marmotta's LDCache library for
locally caching data that has already been retrieved. By default, the cached
data will only exist during execution of the query. If you want to create
a persistent cache, you can pass a cache directory on the command line:

java -jar ldquery-${project.version}.jar \
     -store /tmp/ldcache \
     -context http://graph.facebook.com/102189213155511 \
     -path "dct:title"




