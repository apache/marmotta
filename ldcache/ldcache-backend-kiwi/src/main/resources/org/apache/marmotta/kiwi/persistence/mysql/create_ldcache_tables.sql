CREATE TABLE seq_ldcache (id BIGINT NOT NULL);
INSERT INTO seq_ldcache(id) VALUES (0);

CREATE TABLE ldcache_entries (
  id           bigint     NOT NULL,
  retrieved_at timestamp  NOT NULL,
  expires_at   timestamp  NOT NULL,
  resource_id  bigint     NOT NULL REFERENCES nodes(id),
  update_count int        NOT NULL DEFAULT 0,
  PRIMARY KEY(id)
);


CREATE INDEX idx_ldcache_expires ON ldcache_entries(expires_at);
CREATE INDEX idx_ldcache_resource ON ldcache_entries(resource_id);

