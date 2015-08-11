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


--creating new type 'geom'
-- 1. rename the enum type 
alter type nodetype rename to nodetype_old;
-- 2. create new type
CREATE TYPE nodetype AS ENUM ('uri','bnode','string','int','double','date','boolean','geom');
-- 3. rename column(s) which uses our enum type
alter table nodes rename column ntype to ntype_old;
-- 4. add new column of new type
alter table nodes add ntype nodetype;
-- 5. copy values to the new column
update nodes set ntype = ntype_old::text::nodetype;
-- 6. remove old column and type
alter table nodes drop column ntype_old;

-- 3. delete old enum type
drop type nodetype_old;


--ALTER TYPE nodetype ADD VALUE 'geom'  AFTER  'boolean';


--necessary for use spatial queries
CREATE EXTENSION IF NOT EXISTS POSTGIS;

--adding geometry colum
ALTER TABLE nodes ADD COLUMN gvalue geometry;

UPDATE METADATA SET mvalue = '5' WHERE mkey = 'version';