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

DROP INDEX IF EXISTS idx_node_content;
DROP INDEX IF EXISTS idx_literal_lang;

DROP INDEX IF EXISTS idx_triples_s;
DROP INDEX IF EXISTS idx_triples_o;
DROP INDEX IF EXISTS idx_triples_sp;
DROP INDEX IF EXISTS idx_triples_po;
DROP INDEX IF EXISTS idx_triples_spo;
DROP INDEX IF EXISTS idx_triples_cs;
DROP INDEX IF EXISTS idx_triples_csp;
DROP INDEX IF EXISTS idx_triples_cspo;

DROP INDEX IF EXISTS idx_namespaces_uri;
DROP INDEX IF EXISTS idx_namespaces_prefix;


DROP TABLE IF EXISTS triples;
DROP TABLE IF EXISTS namespaces;
DROP TABLE IF EXISTS nodes;
DROP TABLE IF EXISTS metadata;

DROP SEQUENCE IF EXISTS seq_nodes;
DROP SEQUENCE IF EXISTS seq_triples;
DROP SEQUENCE IF EXISTS seq_namespaces;

DROP ALL OBJECTS DELETE FILES;