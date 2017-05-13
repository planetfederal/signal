--  Copyright 2016-2017 Boundless, http://boundlessgeo.com
--
--  Licensed under the Apache License, Version 2.0 (the "License");
--  you may not use this file except in compliance with the License.
--  You may obtain a copy of the License at
--
--  http://www.apache.org/licenses/LICENSE-2.0
--
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--  See the License for the specific language governing permissions and
--  limitations under the License.

--CREATE EXTENSION IF NOT EXISTS pgcrypto;
--CREATE EXTENSION IF NOT EXISTS postgis;

CREATE SCHEMA IF NOT EXISTS signal;
SET search_path=signal,public;

CREATE OR REPLACE FUNCTION signal.update_updated_at_column()
        RETURNS TRIGGER AS '
    BEGIN
        NEW.updated_at = NOW();
        RETURN NEW;
    END;
' LANGUAGE 'plpgsql';

CREATE TABLE IF NOT EXISTS signal.triggers
(
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  description TEXT,
  name TEXT,
  persistent BOOL,
  repeated BOOL,
  reducers json,
  filters json,
  predicates []
  source json,
  sink json,
  created_at timestamp DEFAULT NOW(),
  updated_at timestamp DEFAULT NOW(),
  deleted_at timestamp with time zone
)
WITH (
  OIDS=FALSE
);

CREATE TRIGGER update_updated_at_triggers
    BEFORE UPDATE ON signal.triggers FOR EACH ROW EXECUTE
    PROCEDURE signal.update_updated_at_column();

CREATE TABLE IF NOT EXISTS signal.notifications
(
  id SERIAL PRIMARY KEY,
  trigger_id UUID,
  created_at timestamp DEFAULT NOW(),
  updated_at timestamp DEFAULT NOW(),
  deleted_at timestamp with time zone,
  device_id integer UNIQUE,
  CONSTRAINT notifications_triggers_fkey FOREIGN KEY (trigger_id)
    REFERENCES signal.triggers(id) MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE SET NULL
)
WITH (
  OIDS=FALSE
);

CREATE TRIGGER update_updated_at_notifications
    BEFORE UPDATE ON signal.notifications FOR EACH ROW EXECUTE
    PROCEDURE signal.update_updated_at_column();


CREATE TYPE signal.message_type AS ENUM ('trigger');

CREATE TABLE signal.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    info json,
    type signal.message_type,
    created_at timestamp DEFAULT NOW()
) WITH (
    OIDS=FALSE
);

ALTER TABLE signal.notifications ADD COLUMN recipient text;
ALTER TABLE signal.notifications ADD COLUMN message_id UUID;
ALTER TABLE signal.notifications ADD CONSTRAINT notifications_messages_fkey
    FOREIGN KEY (message_id) REFERENCES signal.messages (id) MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE signal.notifications DROP COLUMN IF EXISTS device_id;
ALTER TABLE signal.notifications DROP COLUMN IF EXISTS trigger_id;
ALTER TABLE signal.notifications ADD COLUMN sent timestamp DEFAULT NULL;
ALTER TABLE signal.notifications ADD COLUMN delivered timestamp DEFAULT NULL;