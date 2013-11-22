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

-- a table for representing metadata about complete reasoner programs
CREATE TABLE reasoner_programs (
  id          bigint         NOT NULL,
  name        varchar(64) NOT NULL,
  description TEXT,
  PRIMARY KEY (id)
)  CHARACTER SET utf8 COLLATE utf8_bin;

-- reasoner rules consist of an id, a program name, a rule name, and the parsable content in sKWRL syntax
CREATE TABLE reasoner_rules (
  id          bigint         NOT NULL,
  name        varchar(64),
  description TEXT,
  body        TEXT        NOT NULL,
  createdAt   TIMESTAMP   NOT NULL,
  PRIMARY KEY (id)
)  CHARACTER SET utf8 COLLATE utf8_bin;


-- a table for representing namespace configurations per reasoner program
CREATE TABLE reasoner_program_namespaces (
  program_id  bigint           NOT NULL REFERENCES reasoner_programs(id) ON DELETE CASCADE,
  ns_prefix   VARCHAR(64)   NOT NULL,
  ns_uri      VARCHAR(2048) NOT NULL,
  PRIMARY KEY (program_id, ns_prefix)
)  CHARACTER SET utf8 COLLATE utf8_bin;

-- join table between programs and rules
CREATE TABLE reasoner_program_rules (
  program_id     bigint         NOT NULL REFERENCES reasoner_programs(id) ON DELETE CASCADE,
  rule_id        bigint         NOT NULL REFERENCES reasoner_rules(id) ON DELETE CASCADE
)  CHARACTER SET utf8 COLLATE utf8_bin;


-- justifications: support a single triple by other base triples and rules
CREATE TABLE reasoner_justifications (
  id        bigint      NOT NULL,
  triple_id bigint      NOT NULL REFERENCES triples(id),
  createdAt TIMESTAMP   NOT NULL,
  PRIMARY KEY (id)
)  CHARACTER SET utf8 COLLATE utf8_bin;

-- join table from justifications to supporting triples
CREATE TABLE reasoner_just_supp_triples (
  justification_id  bigint NOT NULL REFERENCES reasoner_justifications(id) ON DELETE CASCADE,
  triple_id         bigint NOT NULL REFERENCES triples(id) ON DELETE CASCADE
)  CHARACTER SET utf8 COLLATE utf8_bin;


-- join table from justifications to supporting rules
CREATE TABLE reasoner_just_supp_rules (
  justification_id  bigint NOT NULL REFERENCES reasoner_justifications(id) ON DELETE CASCADE,
  rule_id           bigint NOT NULL REFERENCES reasoner_rules(id) ON DELETE CASCADE
)  CHARACTER SET utf8 COLLATE utf8_bin;


CREATE INDEX idx_justification_triple ON reasoner_justifications (triple_id);
CREATE INDEX idx_just_supp_rules_just ON reasoner_just_supp_rules(justification_id);
CREATE INDEX idx_just_supp_rules_rule ON reasoner_just_supp_rules(rule_id);
CREATE INDEX idx_just_supp_triples_just ON reasoner_just_supp_triples(justification_id);
CREATE INDEX idx_just_supp_triples_triple ON reasoner_just_supp_triples(triple_id);
