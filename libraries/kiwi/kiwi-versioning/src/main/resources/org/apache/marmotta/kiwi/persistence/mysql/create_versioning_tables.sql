CREATE TABLE seq_versions (id BIGINT NOT NULL);
INSERT INTO seq_versions(id) VALUES (0);

-- Sequences in MySQL:
-- UPDATE sequence SET id=LAST_INSERT_ID(id+1);
-- SELECT LAST_INSERT_ID();


CREATE TABLE versions (
  id        bigint     NOT NULL,
  creator   bigint     REFERENCES nodes(id),
  createdAt timestamp  NOT NULL DEFAULT now(),
  PRIMARY KEY(id)
);

-- join table: from version to the added triples
CREATE TABLE versions_added (
  version_id  bigint  REFERENCES versions(id),
  triple_id   bigint REFERENCES triples(id)
);

-- join table: from version to the removed triples
CREATE TABLE versions_removed (
  version_id  bigint  REFERENCES versions(id),
  triple_id   bigint REFERENCES triples(id)
);

CREATE INDEX idx_versions_added ON versions_added(version_id);
CREATE INDEX idx_versions_removed ON versions_removed(version_id);
CREATE INDEX idx_versions_created ON versions(createdAt);

