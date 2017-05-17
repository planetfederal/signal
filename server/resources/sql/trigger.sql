-- name: trigger-list-query
-- Gets list of all active triggers
SELECT * FROM signal.triggers WHERE deleted_at IS NULL

-- name: find-by-id-query
-- gets a trigger by its uuid/primary key
SELECT * FROM signal.triggers WHERE deleted_at IS NULL AND id = :id

-- name: insert-trigger<!
-- inserts a new trigger definition
INSERT INTO signal.triggers
(name,description,repeated,persistent,filters,
reducers,predicates,sink,source,created_at,updated_at)
VALUES
(:name,:description,:repeated,:persistent,
:filters::json,:reducers::json,:predicates::json,
:sink::json,:source::json,NOW(),NOW());

-- name: update-trigger<!
-- updates definition and recipients
UPDATE signal.triggers SET
name = :name,
description = :description,
repeated = :repeated,
persistent = :persistent,
source = :source::json,
filters = :filters::json,
reducers = :reducers::json,
predicates = :predicates::json,
sink = :sink::json,
source = :source::json,
updated_at = NOW()
WHERE id = :id

-- name: delete-trigger!
-- disables a trigger
UPDATE signal.triggers SET deleted_at = NOW() WHERE id = :id