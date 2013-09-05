UPDATE metadata SET mvalue = '2' WHERE mkey = 'version';

-- update nodes.svalue to longtext
ALTER TABLE nodes MODIFY svalue LONGTEXT NOT NULL;

ALTER TABLE nodes ADD CONSTRAINT nodes_unique UNIQUE (ntype,svalue);

-- drop not null on triples.context
ALTER TABLE triples MODIFY context bigint     REFERENCES nodes(id);
