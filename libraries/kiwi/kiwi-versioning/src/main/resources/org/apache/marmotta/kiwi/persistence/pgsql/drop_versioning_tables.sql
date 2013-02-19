DROP INDEX idx_versions_added;
DROP INDEX idx_versions_removed;
DROP INDEX idx_versions_created;

DROP TABLE IF EXISTS versions_added;
DROP TABLE IF EXISTS versions_removed;
DROP TABLE IF EXISTS versions;

DROP SEQUENCE IF EXISTS seq_versions;
