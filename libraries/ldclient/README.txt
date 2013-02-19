This component will contain all the code necessary for accessing linked data resources. It will be split into the
following modules:

- ldclient-api: defines the API for accessing LD resources, as well as the model for data providers that wrap
  legacy sources
- ldclient-core: implements the core functionality for accessing LD resources via HTTP and other protocols; offers
  access to the API through a factory
- ldclient-provider-rdf: implements providers for accessing LD resources that already offer data as RDF
- ldclient-provider-xml: implements (abstract) providers for accessing LD resources that offer some form of XML;
  these providers can be subclassed for mapping XPath expressions to RDF properties

In addition, a number of custom providers can be defined.
