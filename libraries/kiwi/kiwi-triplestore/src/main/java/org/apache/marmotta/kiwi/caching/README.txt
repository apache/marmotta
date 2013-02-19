Caching is used for the following tasks:
- cache query results (i.e. calls to listTriples); this caching should address cursor/iterator operations for large
  data sets
- cache individual triples, resources, namespaces for get/create methods
- cache individual triples, resources, namespaces for id-based lookups