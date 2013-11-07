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

CREATE OR REPLACE FUNCTION kiwi_ft_lang(lang_iso TEXT) RETURNS regconfig AS $$
DECLARE
  lang2 TEXT;
BEGIN
  lang2 := lower(left(lang_iso,2));
  CASE lang2
    WHEN 'en' THEN RETURN 'english';
    WHEN 'de' THEN RETURN 'german';
    WHEN 'fr' THEN RETURN 'french';
    WHEN 'it' THEN RETURN 'italian';
    WHEN 'es' THEN RETURN 'spanish';
    WHEN 'pt' THEN RETURN 'portuguese';
    WHEN 'sv' THEN RETURN 'swedish';
    WHEN 'no' THEN RETURN 'norwegian';
    WHEN 'dk' THEN RETURN 'danish';
    WHEN 'nl' THEN RETURN 'dutch';
    WHEN 'ru' THEN RETURN 'russian';
    WHEN 'tr' THEN RETURN 'turkish';
    WHEN 'hu' THEN RETURN 'hungarian';
    WHEN 'fi' THEN RETURN 'finnish';
    ELSE RETURN 'simple';
  END CASE;
END
$$ LANGUAGE plpgsql IMMUTABLE ;

INSERT INTO metadata(mkey,mvalue) VALUES ('ft.lookup','true');

