DROP INDEX idx_justification_triple;
DROP INDEX idx_just_supp_rules_just;
DROP INDEX idx_just_supp_rules_rule;
DROP INDEX idx_just_supp_triples_just;
DROP INDEX idx_just_supp_triples_triple;

DROP TABLE IF EXISTS reasoner_just_supp_rules;
DROP TABLE IF EXISTS reasoner_just_supp_triples;
DROP TABLE IF EXISTS reasoner_justifications;
DROP TABLE IF EXISTS reasoner_program_rules;
DROP TABLE IF EXISTS reasoner_rules;
DROP TABLE IF EXISTS reasoner_program_namespaces;
DROP TABLE IF EXISTS reasoner_programs;

DROP SEQUENCE IF EXISTS seq_rules;
DROP SEQUENCE IF EXISTS seq_justifications;
DROP SEQUENCE IF EXISTS seq_programs;
