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
CREATE TABLE versions (
  id        bigint     NOT NULL,
  creator   bigint     REFERENCES nodes(id),
  createdAt timestamp  NOT NULL DEFAULT now(),
  PRIMARY KEY(id)
);

-- join table: from version to the added triples
CREATE TABLE versions_added (
  version_id  bigint  REFERENCES versions(id),
  triple_id   bigint REFERENCES triples(id)
);

-- join table: from version to the removed triples
CREATE TABLE versions_removed (
  version_id  bigint  REFERENCES versions(id),
  triple_id   bigint REFERENCES triples(id)
);

CREATE INDEX idx_versions_added ON versions_added(version_id);
CREATE INDEX idx_versions_added_tid ON versions_added(triple_id);
CREATE INDEX idx_versions_removed ON versions_removed(version_id);
CREATE INDEX idx_versions_removed_tid ON versions_removed(triple_id);
CREATE INDEX idx_versions_created ON versions(createdAt);

