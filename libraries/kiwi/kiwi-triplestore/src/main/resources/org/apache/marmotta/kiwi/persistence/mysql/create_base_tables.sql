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

CREATE TABLE nodes (
  id        bigint     NOT NULL,
  ntype     char(8)    NOT NULL,
  svalue    longtext   NOT NULL,
  dvalue    double precision,
  ivalue    bigint,
  tvalue    datetime   DEFAULT NULL,
  tzoffset  INT,
  bvalue    boolean,
  ltype     bigint     REFERENCES nodes(id),
  lang      varchar(5),
  createdAt timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_bin  ENGINE=MyISAM;

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
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_bin  ENGINE=InnoDB;

CREATE TABLE namespaces (
  id        bigint        NOT NULL,
  prefix    varchar(256)  NOT NULL,
  uri       varchar(2048) NOT NULL,
  createdAt timestamp     NOT NULL DEFAULT now(),
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_bin  ENGINE=InnoDB;


-- A table for storing metadata about the current database, e.g. version numbers for each table
CREATE TABLE metadata (
  id        integer       NOT NULL AUTO_INCREMENT,
  mkey      varchar(16)   NOT NULL,
  mvalue    varchar(256)  NOT NULL,
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_bin  ENGINE=InnoDB;

-- a table for temporary triple id registrations
CREATE TABLE registry (
  tripleKey BIGINT NOT NULL,
  tripleId  BIGINT NOT NULL,
  txId      BIGINT NOT NULL,
  INDEX USING BTREE(tripleKey),
  INDEX USING BTREE(tripleId),
  INDEX USING BTREE(txId)
) ENGINE=MEMORY;

-- Indexes for accessing nodes and triples efficiently
CREATE INDEX idx_node_content ON nodes(svalue(256));
CREATE INDEX idx_literal_lang ON nodes(lang);

CREATE INDEX idx_triples_p ON triples(predicate);
CREATE INDEX idx_triples_spo ON triples(subject,predicate,object);
CREATE INDEX idx_triples_cspo ON triples(context,subject,predicate,object);

CREATE INDEX idx_namespaces_uri ON namespaces(uri);
CREATE INDEX idx_namespaces_prefix ON namespaces(prefix);

-- insert initial metadata
INSERT INTO metadata(mkey,mvalue) VALUES ('version','4');
INSERT INTO metadata(mkey,mvalue) VALUES ('created',DATE_FORMAT(now(),'%Y-%m-%d %H:%i:%s') );
