DROP INDEX idx_justification_triple ON reasoner_justifications;
DROP INDEX idx_just_supp_rules_just ON reasoner_just_supp_rules;
DROP INDEX idx_just_supp_rules_rule ON reasoner_just_supp_rules;
DROP INDEX idx_just_supp_triples_just ON reasoner_just_supp_triples;
DROP INDEX idx_just_supp_triples_triple ON reasoner_just_supp_triples;

DROP TABLE IF EXISTS reasoner_just_supp_rules;
DROP TABLE IF EXISTS reasoner_just_supp_triples;
DROP TABLE IF EXISTS reasoner_justifications;
DROP TABLE IF EXISTS reasoner_program_rules;
DROP TABLE IF EXISTS reasoner_rules;
DROP TABLE IF EXISTS reasoner_program_namespaces;
DROP TABLE IF EXISTS reasoner_programs;

DROP TABLE IF EXISTS seq_rules;
DROP TABLE IF EXISTS seq_justifications;
DROP TABLE IF EXISTS seq_programs;
