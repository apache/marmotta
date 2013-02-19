
DROP INDEX idx_node_content ON nodes;
DROP INDEX idx_literal_lang ON nodes;

DROP INDEX idx_triples_s ON triples;
DROP INDEX idx_triples_o ON triples;
DROP INDEX idx_triples_sp ON triples;
DROP INDEX idx_triples_po ON triples;
DROP INDEX idx_triples_spo ON triples;
DROP INDEX idx_triples_cs ON triples;
DROP INDEX idx_triples_csp ON triples;
DROP INDEX idx_triples_cspo ON triples;

DROP INDEX idx_namespaces_uri ON namespaces;
DROP INDEX idx_namespaces_prefix ON namespaces;


DROP TABLE IF EXISTS triples;
DROP TABLE IF EXISTS namespaces;
DROP TABLE IF EXISTS nodes;
DROP TABLE IF EXISTS metadata;

DROP TABLE IF EXISTS seq_nodes;
DROP TABLE IF EXISTS seq_triples;
DROP TABLE IF EXISTS seq_namespaces;

