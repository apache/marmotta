UPDATE METADATA SET mvalue = '2' WHERE mkey = 'version';

-- drop not null on triples.context
ALTER TABLE triples ALTER COLUMN context DROP NOT NULL;