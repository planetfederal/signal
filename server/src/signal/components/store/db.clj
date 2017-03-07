;; Copyright 2016-2017 Boundless, http://boundlessgeo.com
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns signal.components.store.db
  (:require [signal.db.conn :as db]
            [signal.util.db :as dbutil]
            [yesql.core :refer [defqueries]]
            [signal.specs.store]
            [clojure.data.json :as json]
            [clojure.spec :as s]
            [camel-snake-kebab.core :refer :all]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [signal.entity.store :refer :all]))

;; define sql queries as functions in this namespace
(defqueries "sql/store.sql" {:connection db/db-spec})

(def uuid-regex #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

(defn- sanitize [store]
  (dissoc store :created_at :updated_at :deleted_at))

(defn map->entity [t]
  (-> t
      (cond-> (nil? (:options t)) (assoc :options nil))
      (cond-> (some? (:options t)) (assoc :options (json/write-str (:options t))))
      (cond-> (nil? (:style t)) (assoc :style nil))
      (cond-> (some? (:style t)) (assoc :style (json/write-str (:style t))))))
      ;(assoc :default_layers (dbutil/->StringArray (:default_layers t)))))

(defn row-fn [row]
  (-> row
      (dbutil/sqlarray->vec :default_layers)
      (dbutil/sqluuid->str :id)))

(def result->map
  {:result-set-fn doall
   :row-fn row-fn
   :identifiers clojure.string/lower-case})

(defn all
  "Lists all the active stores"
  []
  (let [res (store-list-query {} result->map)]
    (map sanitize res)))

(defn find-by-id
  "Gets store by store identifier"
  [id]
  (if (re-matches uuid-regex id)
    (some-> (find-by-id-query {:id (java.util.UUID/fromString id)} result->map)
            (first)
            (sanitize))
    nil))

(defn create
  "Creates a store"
  [t]
  (let [tr (map->entity t)]
    (if-let [new-store (insert-store<! (assoc tr :default_layers
                                              (dbutil/->StringArray (:default_layers tr))))]
      (sanitize (assoc t :id (.toString (:id new-store))))
      nil)))

(defn modify
  "Update a data store"
  [id t]
  (let [entity (map->entity (assoc t :id (java.util.UUID/fromString id)))
        updated-store (update-store<! (assoc entity :default_layers
                                             (dbutil/->StringArray (:default_layers entity))))]
    (sanitize (row-fn updated-store))))

(defn delete
  "Deactivates a store"
  [id]
  (delete-store! {:id (java.util.UUID/fromString id)}))

(s/fdef all
        :args empty?
        :ret (s/coll-of :signal.specs.store/store-spec))

(s/fdef create
        :args (s/cat :t :signal.specs.store/store-spec)
        :ret (s/spec :signal.specs.store/store-spec))
