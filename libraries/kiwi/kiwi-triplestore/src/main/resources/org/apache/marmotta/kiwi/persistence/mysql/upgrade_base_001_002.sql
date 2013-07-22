UPDATE metadata SET mvalue = '2' WHERE mkey = 'version';

ALTER TABLE nodes ADD CONSTRAINT nodes_unique UNIQUE (ntype,svalue);
