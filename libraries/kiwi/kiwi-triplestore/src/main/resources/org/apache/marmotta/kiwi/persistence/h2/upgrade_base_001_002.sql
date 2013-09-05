UPDATE METADATA SET mvalue = '2' WHERE mkey = 'version';

-- drop not null on triples.context
ALTER TABLE triples ALTER COLUMN context bigint;
ALTER TABLE nodes ALTER COLUMN svalue varchar(2147483647) NOT NULL;
