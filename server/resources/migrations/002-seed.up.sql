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

INSERT INTO signal.users (name,email,password) VALUES ('Admin Person','admin@something.com','bcrypt+sha512$4588e3bec69d6cd42533a71ac375c2e7$12$a56c911a6bd02cb7a872fc6d5d6876462b99c44f4bdc8218');
INSERT INTO signal.organizations (name) VALUES ('My Organization Name');
INSERT INTO signal.teams (name, organization_id) values ('My First Group Name', 1);
INSERT INTO signal.teams (name, organization_id) values ('Another Group Name', 1);
INSERT INTO signal.user_team (user_id, team_id) values (1,1);
INSERT INTO signal.user_team (user_id, team_id) values (1,2);
