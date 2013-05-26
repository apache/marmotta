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
CREATE TABLE seq_nodes (id BIGINT NOT NULL);
INSERT INTO seq_nodes(id) VALUES (0);

CREATE TABLE seq_triples (id BIGINT NOT NULL);
INSERT INTO seq_triples VALUES (0);

CREATE TABLE seq_namespaces (id BIGINT NOT NULL);
INSERT INTO seq_namespaces(id) VALUES (0);

-- Sequences in MySQL:
-- UPDATE sequence SET id=LAST_INSERT_ID(id+1);
-- SELECT LAST_INSERT_ID();


CREATE TABLE nodes (
  id        bigint     NOT NULL,
  ntype     char(8)    NOT NULL,
  svalue    text       NOT NULL,
  dvalue    double precision,
  ivalue    bigint,
  tvalue    datetime   DEFAULT NULL,
  bvalue    boolean,
  ltype     bigint     REFERENCES nodes(id),
  lang      varchar(5),
  createdAt timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE triples (
  id        bigint     NOT NULL,
  subject   bigint     NOT NULL REFERENCES nodes(id),
  predicate bigint     NOT NULL REFERENCES nodes(id),
  object    bigint     NOT NULL REFERENCES nodes(id),
  context   bigint     NOT NULL REFERENCES nodes(id),
  creator   bigint     REFERENCES nodes(id),
  inferred  boolean    DEFAULT false,
  deleted   boolean    DEFAULT false,
  createdAt timestamp  NOT NULL DEFAULT now(),
  deletedAt timestamp,
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE namespaces (
  id        bigint        NOT NULL,
  prefix    varchar(256)  NOT NULL,
  uri       varchar(2048) NOT NULL,
  createdAt timestamp     NOT NULL DEFAULT now(),
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_bin;


-- A table for storing metadata about the current database, e.g. version numbers for each table
CREATE TABLE metadata (
  id        integer       NOT NULL AUTO_INCREMENT,
  mkey      varchar(16)   NOT NULL,
  mvalue    varchar(256)  NOT NULL,
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_bin;

-- Indexes for accessing nodes and triples efficiently
CREATE INDEX idx_node_content ON nodes(svalue(256));
CREATE INDEX idx_literal_lang ON nodes(lang);

-- CREATE INDEX idx_triples_s ON triples(subject);
CREATE INDEX idx_triples_o ON triples(object);
-- CREATE INDEX idx_triples_sp ON triples(subject,predicate);
CREATE INDEX idx_triples_po ON triples(predicate,object);
CREATE INDEX idx_triples_spo ON triples(subject,predicate,object);
-- CREATE INDEX idx_triples_cs ON triples(context,subject);
-- CREATE INDEX idx_triples_csp ON triples(context,subject,predicate);
CREATE INDEX idx_triples_cspo ON triples(context,subject,predicate,object);

CREATE INDEX idx_namespaces_uri ON namespaces(uri);
CREATE INDEX idx_namespaces_prefix ON namespaces(prefix);

-- insert initial metadata
INSERT INTO metadata(mkey,mvalue) VALUES ('version','2');
INSERT INTO metadata(mkey,mvalue) VALUES ('created',DATE_FORMAT(now(),'%Y-%m-%d %H:%i:%s') );
