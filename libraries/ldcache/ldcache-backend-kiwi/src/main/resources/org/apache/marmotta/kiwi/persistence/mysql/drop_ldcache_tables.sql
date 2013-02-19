DROP INDEX idx_ldcache_expires ON ldcache_entries;
DROP INDEX idx_ldcache_resource ON ldcache_entries;

DROP TABLE IF EXISTS ldcache_entries;
DROP TABLE IF EXISTS seq_ldcache;
