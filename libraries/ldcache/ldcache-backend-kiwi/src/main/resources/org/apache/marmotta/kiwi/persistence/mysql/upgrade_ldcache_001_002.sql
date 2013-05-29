ALTER TABLE ldcache_entries ADD COLUMN triple_count int NOT NULL DEFAULT 0;
UPDATE
  ldcache_entries
SET triple_count = (SELECT count(*) as numTriples FROM triples WHERE subject = ldcache_entries.resource_id and deleted = false)