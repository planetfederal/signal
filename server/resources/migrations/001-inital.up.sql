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


DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role_type') THEN
        CREATE TYPE signal.user_role_type AS ENUM ('user','admin');
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role_type') THEN
        CREATE TYPE signal.user_role_type AS ENUM ('user','admin');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS signal.stores
(
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  store_type TEXT,
  version TEXT,
  uri TEXT,
  name TEXT,
  default_layers TEXT[],
  created_at timestamp DEFAULT NOW(),
  updated_at timestamp DEFAULT NOW(),
  deleted_at timestamp with time zone
)
WITH (
  OIDS=FALSE
);

CREATE TRIGGER update_updated_at_stores
    BEFORE UPDATE ON signal.stores FOR EACH ROW EXECUTE
    PROCEDURE signal.update_updated_at_column();

CREATE TABLE IF NOT EXISTS signal.users (
  id            serial PRIMARY KEY,
  name          TEXT NOT NULL CHECK (name <> ''),
  email         TEXT NOT NULL UNIQUE,
  role          user_role_type,
  created_at    timestamp DEFAULT NOW(),
  updated_at    timestamp DEFAULT NOW(),
  deleted_at    timestamp with time zone,
  password      TEXT NOT NULL
);

CREATE TRIGGER update_updated_at_users
    BEFORE UPDATE ON signal.users FOR EACH ROW EXECUTE
    PROCEDURE signal.update_updated_at_column();
CREATE TABLE IF NOT EXISTS signal.triggers
(
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT,
  stores TEXT[],
  description TEXT,
  recipients json,
  rules json,
  repeated BOOL,
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
