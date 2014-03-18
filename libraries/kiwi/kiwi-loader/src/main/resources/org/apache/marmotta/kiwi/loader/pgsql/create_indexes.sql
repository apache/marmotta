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

CREATE INDEX idx_triples_p ON triples(predicate) WHERE deleted = false;
CREATE INDEX idx_triples_spo ON triples(subject,predicate,object) WHERE deleted = false;
CREATE INDEX idx_triples_cspo ON triples(context,subject,predicate,object) WHERE deleted = false;
CREATE INDEX idx_node_dcontent ON nodes(dvalue) WHERE dvalue IS NOT NULL;
CREATE INDEX idx_node_icontent ON nodes(ivalue) WHERE ivalue IS NOT NULL;

ALTER TABLE triples ENABLE RULE triples_ignore_duplicates;

ALTER TABLE triples
ADD CONSTRAINT triples_subject_fkey FOREIGN KEY (subject)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE triples
ADD CONSTRAINT triples_predicate_fkey FOREIGN KEY (predicate)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE triples
ADD CONSTRAINT triples_object_fkey FOREIGN KEY (object)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE triples
ADD CONSTRAINT triples_creator_fkey FOREIGN KEY (creator)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE triples
ADD CONSTRAINT triples_context_fkey FOREIGN KEY (context)
REFERENCES nodes (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;
