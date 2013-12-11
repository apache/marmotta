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

DROP INDEX idx_node_content ON nodes;
DROP INDEX idx_literal_lang ON nodes;

DROP INDEX idx_triples_p ON triples;
DROP INDEX idx_triples_spo ON triples;
DROP INDEX idx_triples_cspo ON triples;

DROP INDEX idx_namespaces_uri ON namespaces;
DROP INDEX idx_namespaces_prefix ON namespaces;

DROP TABLE IF EXISTS triples;
DROP TABLE IF EXISTS namespaces;
DROP TABLE IF EXISTS nodes;
DROP TABLE IF EXISTS metadata;
DROP TABLE IF EXISTS registry;

