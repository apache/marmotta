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

DROP INDEX IF EXISTS idx_triples_p;
DROP INDEX IF EXISTS idx_triples_spo;
DROP INDEX IF EXISTS idx_triples_cspo;
DROP INDEX IF EXISTS idx_node_dcontent;
DROP INDEX IF EXISTS idx_node_icontent;

ALTER TABLE triples DISABLE RULE triples_ignore_duplicates;


ALTER TABLE triples DROP CONSTRAINT triples_subject_fkey;
ALTER TABLE triples DROP CONSTRAINT triples_predicate_fkey;
ALTER TABLE triples DROP CONSTRAINT triples_object_fkey;
ALTER TABLE triples DROP CONSTRAINT triples_creator_fkey;
ALTER TABLE triples DROP CONSTRAINT triples_context_fkey;