-- name: input-list-query
-- Gets list of all active processors
SELECT * FROM signal.inputs WHERE deleted_at IS NULL

-- name: find-input-by-id-query
-- gets a processor by its uuid/primary key
SELECT * FROM signal.inputs WHERE deleted_at IS NULL AND id = :id

-- name: insert-input<!
-- inserts a new input definition
INSERT INTO signal.inputs
(name,description,type,definition,created_at,updated_at)
VALUES (:name,:description,:type,:definition::json,NOW(),NOW());

-- name: update-input<!
-- updates definition and recipients
UPDATE signal.inputs SET
name = :name,
description = :description,
type = :type,
definition = :definition::json,
updated_at = NOW()
WHERE id = :id

-- name: delete-input!
-- disables an input
UPDATE signal.inputs SET deleted_at = NOW() WHERE id = :id
