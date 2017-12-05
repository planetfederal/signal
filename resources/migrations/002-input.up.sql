CREATE TABLE IF NOT EXISTS signal.inputs
(
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  created_at timestamp DEFAULT NOW(),
  updated_at timestamp DEFAULT NOW(),
  deleted_at timestamp with time zone,
  name TEXT,
  description TEXT,
  type TEXT,
  definition JSON
)
WITH (
  OIDS=FALSE
);
