-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
CREATE TYPE nodetype AS ENUM ('uri','bnode','string','int','double','date','boolean');

-- requires super user privileges:
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE nodes (
  id        bigint     NOT NULL,
  ntype     nodetype   NOT NULL,
  svalue    text       NOT NULL,
  dvalue    double precision,
  ivalue    bigint,
  tvalue    timestamp,
  tzoffset  INT4,
  bvalue    boolean,
  ltype     bigint     REFERENCES nodes(id),
  lang      varchar(5),
  createdAt timestamp  NOT NULL DEFAULT now(),
  PRIMARY KEY(id)
);

CREATE TABLE triples (
  id        bigint     NOT NULL,
  subject   bigint     NOT NULL REFERENCES nodes(id),
  predicate bigint     NOT NULL REFERENCES nodes(id),
  object    bigint     NOT NULL REFERENCES nodes(id),
  context   bigint     REFERENCES nodes(id),
  creator   bigint     REFERENCES nodes(id),
  inferred  boolean    DEFAULT false,
  deleted   boolean    DEFAULT false,
  createdAt timestamp  NOT NULL DEFAULT now(),
  deletedAt timestamp,
  PRIMARY KEY(id),
  CHECK ( (deleted AND deletedAt IS NOT NULL) OR ((NOT deleted) AND deletedAt IS NULL) )
);

CREATE TABLE namespaces (
  id        bigint        NOT NULL,
  prefix    varchar(256)  NOT NULL,
  uri       varchar(2048) NOT NULL,
  createdAt timestamp  NOT NULL DEFAULT now(),
  PRIMARY KEY(id)
);

-- A table for storing metadata about the current database, e.g. version numbers for each table
CREATE TABLE metadata (
  id        serial        NOT NULL,
  mkey      varchar(16)   NOT NULL,
  mvalue    varchar(256)  NOT NULL,
  PRIMARY KEY(id)
);


-- a table for temporary triple id registrations
CREATE UNLOGGED TABLE registry (
  tripleKey BIGINT NOT NULL,
  tripleId  BIGINT NOT NULL,
  txId      BIGINT NOT NULL
);
CREATE INDEX idx_reg_triple ON registry(tripleId);
CREATE INDEX idx_reg_key ON registry(tripleKey);
CREATE INDEX idx_reg_tx ON registry(txId);

-- Indexes for accessing nodes and triples efficiently
CREATE INDEX idx_node_content ON nodes USING hash(svalue);
CREATE INDEX idx_node_dcontent ON nodes(dvalue) WHERE dvalue IS NOT NULL;
CREATE INDEX idx_node_icontent ON nodes(ivalue) WHERE ivalue IS NOT NULL;
CREATE INDEX idx_node_tcontent ON nodes(tvalue) WHERE tvalue IS NOT NULL;
CREATE INDEX idx_literal_lang ON nodes(lang);

CREATE INDEX idx_triples_p ON triples(predicate) WHERE deleted = false;
CREATE INDEX idx_triples_spo ON triples(subject,predicate,object) WHERE deleted = false;
CREATE INDEX idx_triples_cspo ON triples(context,subject,predicate,object) WHERE deleted = false;


CREATE INDEX idx_namespaces_uri ON namespaces(uri);
CREATE INDEX idx_namespaces_prefix ON namespaces(prefix);


-- a rule to ignore duplicate inserts into triple table
CREATE OR REPLACE RULE "triples_ignore_duplicates" AS
ON INSERT TO triples
  WHERE EXISTS(
      SELECT 1 FROM triples
      WHERE id = NEW.id
  )
DO INSTEAD NOTHING;


-- a function for cleaning up table rows without incoming references

-- insert initial metadata
INSERT INTO metadata(mkey,mvalue) VALUES ('version','4');
INSERT INTO metadata(mkey,mvalue) VALUES ('created',to_char(now(),'yyyy-MM-DD HH:mm:ss TZ') );