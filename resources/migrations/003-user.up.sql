CREATE SCHEMA IF NOT EXISTS signal;
SET search_path=signal,public;


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
