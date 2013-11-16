DROP INDEX IF EXISTS idx_triples_op;
DROP INDEX IF EXISTS idx_triples_spo;
DROP INDEX IF EXISTS idx_triples_cspo;

ALTER TABLE triples DISABLE RULE triples_ignore_duplicates;


ALTER TABLE triples DROP CONSTRAINT triples_subject_fkey;
ALTER TABLE triples DROP CONSTRAINT triples_predicate_fkey;
ALTER TABLE triples DROP CONSTRAINT triples_object_fkey;
ALTER TABLE triples DROP CONSTRAINT triples_creator_fkey;
ALTER TABLE triples DROP CONSTRAINT triples_context_fkey;