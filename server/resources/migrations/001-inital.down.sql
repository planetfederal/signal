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

DROP TABLE IF EXISTS signal.organizations CASCADE;
DROP TRIGGER IF EXISTS update_updated_at_organizations ON signal.organizations;
DROP TABLE IF EXISTS signal.teams CASCADE;
DROP TRIGGER IF EXISTS update_updated_at_teams ON signal.teams;
DROP TABLE IF EXISTS signal.stores CASCADE;
DROP TRIGGER IF EXISTS update_updated_at_stores ON signal.stores;
DROP TABLE IF EXISTS signal.forms CASCADE;
DROP TRIGGER IF EXISTS update_updated_at_forms ON signal.forms;
DROP TABLE IF EXISTS signal.devices CASCADE;
DROP TRIGGER IF EXISTS update_updated_at_devices ON signal.devices;
DROP TABLE IF EXISTS signal.device_locations CASCADE;
DROP TRIGGER IF EXISTS update_updated_at_device_locations ON signal.device_locations;
DROP TABLE IF EXISTS signal.form_data CASCADE;
DROP TRIGGER IF EXISTS update_updated_at_form_data ON signal.form_data;
DROP TABLE IF EXISTS signal.form_fields CASCADE;
DROP TRIGGER IF EXISTS update_updated_at_form_fields ON signal.form_fields;
DROP TABLE IF EXISTS signal.user_team CASCADE;
DROP TABLE IF EXISTS signal.users CASCADE;
DROP TYPE IF EXISTS signal.form_type;
DROP TRIGGER IF EXISTS update_updated_at_users ON signal.users;
DROP TABLE IF EXISTS signal.triggers;
DROP TRIGGER IF EXISTS update_updated_at_triggers ON signal.triggers;
DROP FUNCTION IF EXISTS signal.update_updated_at_column();

