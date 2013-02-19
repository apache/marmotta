This components implements Linked Data Caching support in conjunction with the Linked Data Client Library.
It provides the following modules:
- ldcache-api: interfaces and data model for Linked Data Caching
- ldcache-core: core library implementation, manages the caching functionality
- ldcache-sail: Sesame StackedSail that can be used for transparently retrieving Linked Data Resources when
  requested through the Sesame API
- ldcache-backend-kiwi: caching backend based on the KiWi triplestore; allows persisting caching information
  in the JDBC database used by KiWi

Other backends can be implemented as needed.