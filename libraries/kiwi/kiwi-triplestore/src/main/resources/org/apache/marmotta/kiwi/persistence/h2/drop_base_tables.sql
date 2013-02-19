
DROP INDEX IF EXISTS idx_node_content;
DROP INDEX idx_literal_lang;

DROP INDEX IF EXISTS idx_triples_s;
DROP INDEX IF EXISTS idx_triples_o;
DROP INDEX IF EXISTS idx_triples_sp;
DROP INDEX IF EXISTS idx_triples_po;
DROP INDEX IF EXISTS idx_triples_spo;
DROP INDEX IF EXISTS idx_triples_cs;
DROP INDEX IF EXISTS idx_triples_csp;
DROP INDEX IF EXISTS idx_triples_cspo;

DROP INDEX IF EXISTS idx_namespaces_uri;
DROP INDEX IF EXISTS idx_namespaces_prefix;


DROP TABLE IF EXISTS triples;
DROP TABLE IF EXISTS namespaces;
DROP TABLE IF EXISTS nodes;
DROP TABLE IF EXISTS metadata;

DROP SEQUENCE IF EXISTS seq_nodes;
DROP SEQUENCE IF EXISTS seq_triples;
DROP SEQUENCE IF EXISTS seq_namespaces;

DROP ALL OBJECTS DELETE FILES;