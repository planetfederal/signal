-- name: store-list-query
-- gets all the stores
SELECT *
FROM signal.stores
WHERE deleted_at IS NULL;

-- name: find-by-id-query
-- gets store by id
SELECT *
FROM signal.stores
WHERE id = :id AND deleted_at IS NULL

-- name: insert-store<!
-- creates a new store
INSERT INTO signal.stores (
name,  store_type,  version,  uri,
options, default_layers, created_at,
updated_at)
VALUES (:name, :store_type, :version,
:uri, :options::json, :default_layers,
NOW(), NOW())

-- name: update-store<!
-- updates store
UPDATE signal.stores SET
name = :name,
store_type = :store_type,
version = :version,
uri = :uri,
options = :options::json,
style = :style::json,
default_layers = :default_layers,
updated_at = NOW()
WHERE id = :id

-- name: delete-store!
-- deletes the store
UPDATE signal.stores SET deleted_at = NOW() WHERE id = :id
