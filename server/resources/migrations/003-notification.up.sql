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
