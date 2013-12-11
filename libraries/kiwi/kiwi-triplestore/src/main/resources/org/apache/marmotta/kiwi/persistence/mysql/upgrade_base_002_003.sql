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

UPDATE metadata SET mvalue = '3' WHERE mkey = 'version';

DROP INDEX idx_triples_op ON triples;
CREATE INDEX idx_triples_p ON triples(predicate);


-- a table for temporary triple id registrations
CREATE TABLE registry (
  tripleKey BIGINT NOT NULL,
  tripleId  BIGINT NOT NULL,
  txId      BIGINT NOT NULL,
  INDEX USING BTREE(tripleKey),
  INDEX USING BTREE(tripleId),
  INDEX USING BTREE(txId)
) ENGINE=MEMORY;
