CREATE INDEX idx_triples_op ON triples(object,predicate) WHERE deleted = false;
CREATE INDEX idx_triples_spo ON triples(subject,predicate,object) WHERE deleted = false;
CREATE INDEX idx_triples_cspo ON triples(context,subject,predicate,object) WHERE deleted = false;
CREATE INDEX idx_node_dcontent ON nodes(dvalue) WHERE dvalue IS NOT NULL;
CREATE INDEX idx_node_icontent ON nodes(ivalue) WHERE ivalue IS NOT NULL;

ALTER TABLE triples ENABLE RULE triples_ignore_duplicates;

ALTER TABLE triples
ADD CONSTRAINT triples_subject_fkey FOREIGN KEY (subject)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE triples
ADD CONSTRAINT triples_predicate_fkey FOREIGN KEY (predicate)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE triples
ADD CONSTRAINT triples_object_fkey FOREIGN KEY (object)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE triples
ADD CONSTRAINT triples_creator_fkey FOREIGN KEY (creator)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE triples
ADD CONSTRAINT triples_context_fkey FOREIGN KEY (context)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;
