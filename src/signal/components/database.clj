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

(ns signal.components.database
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojure.set :refer [rename-keys]]
            [buddy.hashers :as hashers]
            [yesql.core :as ysql]
            [ragtime.repl :as repl]
            [ragtime.jdbc :as rjdbc]
            [clojure.data.json :as json]
            [jdbc.pool.c3p0 :as pool]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as clj-jdbc]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as st]
            [com.stuartsierra.component :as component]
            [signal.specs.processor]))

(def db-creds (or (some-> (System/getenv "VCAP_SERVICES")
                          (json/read-str :key-fn clojure.core/keyword)
                          :pg_95_XL_DEV_CONTENT_001
                          first
                          :credentials)
                  {:db_host  (or (System/getenv "DB_HOST") "localhost")
                   :db_port  5432
                   :db_name  (or (System/getenv "DB_NAME") "signal")
                   :username (or (System/getenv "DB_USER") "signal")
                   :password (or (System/getenv "DB_PASSWORD") "signal")}))

(def db-spec []
  (log/debug "Making db connection to"
             (format "%s:%s/%s" (:db_host db-creds) (:db_port db-creds) (:db_name db-creds)))
  (pool/make-datasource-spec
   {:classname   "org.postgresql.Driver"
    :subprotocol "postgresql"
    :subname     (format "//%s:%s/%s" (:db_host db-creds) (:db_port db-creds) (:db_name db-creds))
    :user        (:username db-creds)
    :password    (:password db-creds)
    :stringtype  "unspecified"
    :max-pool-size     10
    :min-pool-size     2
    :initial-pool-size 2}))

(defn create-schema
  "Creates the Signal schema for Postgres"
  []
  (log/debug "Creating schema if it doesnt exist")
  (clojure.java.jdbc/execute! db-spec ["CREATE SCHEMA IF NOT EXISTS signal"]))

(defn loadconfig []
  (log/debug "Loading database migration config")
  (create-schema)
  {:datastore  (rjdbc/sql-database db-spec {:migrations-table "signal.migrations"})
   :migrations (rjdbc/load-resources "migrations")})

(defn migrate []
  (log/debug "Running database migration")
  (repl/migrate (loadconfig)))

(defn rollback []
  (log/debug "Rolling back database migration")
  (repl/rollback (loadconfig)))

;;;;;;;;;;;;;;;;;SQL;;;;;;;;;;;;;;
(defn- load-sql [db-spec]
  (ysql/defqueries "sql/notification.sql" {:connection db-spec})
  (ysql/defqueries "sql/processor.sql" {:connection db-spec})
  (ysql/defqueries "sql/input.sql" {:connection db-spec})
  (ysql/defqueries "sql/user.sql" {:connection db-spec}))

;;;;;;;;;;;SANITIZERS;;;;;;;;
(defn sanitize-timestamps [v]
  (dissoc v :updated_at :deleted_at))

(defn sanitize-user [u]
  (dissoc (sanitize-timestamps u) :password :created_at))

(defn- sanitize [processor]
  (dissoc processor :created_at :updated_at :deleted_at))

(defn- processor-entity->map [t]
  (-> t
      (assoc :id (.toString (:id t)))
      (assoc :created_at (.toString (:created_at t)))
      (assoc :updated_at (.toString (:updated_at t)))
      (rename-keys {:input_ids  :input-ids
                    :created_at :created-at
                    :updated_at :updated-at
                    :deleted_at :deleted-at})))

(defn- input-entity->map [i]
  (-> i
      (assoc :id (.toString (:id i)))
      (assoc :created_at (.toString (:created_at i)))
      (assoc :updated_at (.toString (:updated_at i)))
      (rename-keys {:created_at :created-at
                    :updated_at :updated-at
                    :deleted_at :deleted-at})))

(defn- row-fn
  "Modifies the row result while the ResultSet is open. This method
  is required for accessing methods that are only available while the
  PG ResultSet is open"
  [row]
  (-> row
      (assoc :input_ids (vec (.getArray (:input_ids row))))))

(def result->map
  {:result-set-fn doall
   :row-fn        row-fn
   :identifiers   clojure.string/lower-case})

;;;;;;;;;;;;UTILS;;;;;;;;;;;;;;;;;;
(deftype StringArray [items]
  clj-jdbc/ISQLParameter
  (set-parameter [_ stmt ix]
    (let [as-array (into-array Object items)
          jdbc-array (.createArrayOf (.getConnection stmt) "text" as-array)]
      (.setArray stmt ix jdbc-array))))

(deftype UUIDArray [items]
  clj-jdbc/ISQLParameter
  (set-parameter [_ stmt ix]
    (let [as-array (into-array Object items)
          jdbc-array (.createArrayOf (.getConnection stmt) "UUID" as-array)]
      (.setArray stmt ix jdbc-array))))

(deftype UUIDArray [items]
  clj-jdbc/ISQLParameter)

(defn sqluuid->str [row col-name]
  (if-let [r (col-name row)]
    (assoc row col-name (if (instance? java.util.UUID r) (.toString r) r))
    row))

(defn sqlarray->vec [row col-name]
  (if-let [r (col-name row)]
    (assoc row col-name (vec (.getArray r)))
    row))

(extend-type java.sql.Timestamp
  clojure.data.json/JSONWriter
  (-write [date out]
    (clojure.data.json/-write (str date) out)))

(extend-type java.util.UUID
  clojure.data.json/JSONWriter
  (-write [uuid out]
    (clojure.data.json/-write (str uuid) out)))

(extend-type org.postgresql.util.PGobject
  clj-jdbc/IResultSetReadColumn
  (result-set-read-column [val rsmeta idx]
    (let [colType (.getColumnTypeName rsmeta idx)]
      (if (contains? #{"json" "jsonb"} colType)
        (json/read-str (.getValue val) :key-fn clojure.core/keyword)
        val))))

;;;;;;;;;;;;NOTIFICATION;;;;;;;;;;;
(defn- create-message [message-type info]
  (let [info-str (json/write-str info)]
    (insert-message<!
     {:type message-type :info info-str})))

(defn find-message-by-id [id]
  (find-message-by-id-query {:id id}))

(defn create-notifications
  "Adds a notification to the queue"
  [recipients message-type info]
  (let [message (create-message message-type info)
        id (:id message)]
    (map #(sanitize-timestamps
           (insert-notification<!
            {:recipient  %
             :message_id id})) recipients)))

(defn create-notification
  [recipient message-type info]
  (sanitize-timestamps
   (insert-notification<!
    {:recipient  recipient
     :message_id (:id (create-message message-type info))})))

(defn unsent
  "List of all the unsent notifications"
  []
  (map sanitize-timestamps (unsent-notifications-list)))

(defn undelivered
  []
  (map sanitize-timestamps (undelivered-notifications-list)))

(defn notifications []
  (map sanitize-timestamps (notifications-list)))

(defn find-notif-by-id [id]
  (some-> (find-notification-by-id-query {:id id})
          first
          sanitize-timestamps))

(defn find-message-by-id [id]
  (some-> (find-message-by-id-query {:id id})
          first))

(defn mark-as-sent [notif-id]
  (mark-as-sent! {:id notif-id}))

(defn mark-as-delivered [notif-id]
  (mark-as-delivered! {:id notif-id}))

;;;;;;;;;;;;TRIGGERS;;;;;;;;;;;
(defn processors
  "Returns all the active processors"
  []
  (log/debug "Fetching all active processors from db")
  (map processor-entity->map (processor-list-query {} result->map)))

(defn processor-by-id
  "Find processor by identifier"
  [id]
  (log/debugf "Finding processors with id %s from db" id)
  (some-> (find-by-id-query {:id (java.util.UUID/fromString id)} result->map)
          first
          processor-entity->map))

(defn map->processor-entity
  [trg]
  (-> (assoc trg :definition (json/write-str (:definition trg)))
      (rename-keys {:input-ids  :input_ids
                    :created-at :created_at
                    :deleted-at :deleted_at
                    :updated-at :updated_at})))

(defn create-processor
  "Creates a processor definition"
  [t]
  (log/debug "Validating processor against spec")
  (do
    (let [entity (map->processor-entity t)
          new-processor (insert-processor<!
                         (assoc entity :input_ids (->StringArray (:input_ids entity))))]
      (processor-entity->map (assoc t :id (:id new-processor)
                                    :created_at (:created_at new-processor)
                                    :updated_at (:updated_at new-processor))))))

(s/fdef create-processor
        :args (s/cat :t :signal.specs.processor/processor-spec)
        :ret :signal.specs.processor/processor-spec)

(defn modify-processor
  "Update processor"
  [id t]
  (let [entity (map->processor-entity (assoc t :id (java.util.UUID/fromString id)))
        updated-processor (update-processor<!
                           (assoc entity
                                  :input_ids
                                  (->StringArray (:input_ids t))))]
    (processor-entity->map (assoc t :id (:id updated-processor)
                                  :created_at (:created_at updated-processor)
                                  :updated_at (:updated_at updated-processor)))))

(defn delete-processor
  "Delete processor"
  [id]
  (delete-processor! {:id (java.util.UUID/fromString id)}))

;;;;;;;;;;;;;;;;INPUT;;;;;;;;;;;;;;
(defn inputs
  "Returns all the active inputs"
  []
  (log/debug "Fetching all active inputs from db")
  (map input-entity->map (input-list-query)))

(defn input-by-id
  "Fetch processor by its identifier"
  [id]
  (log/debugf "Finding input with the id %s from db", id)
  (some-> (find-input-by-id-query {:id (java.util.UUID/fromString id)})
          first
          input-entity->map))

(defn map->input-entity [i]
  (-> (assoc i :definition (json/write-str (:definition i)))
      (rename-keys {:created-at :created_at
                    :deleted-at :deleted_at
                    :updated-at :updated_at})))

(defn create-input
  "Creates an input"
  [i]
  (log/debug "Validating input against spec")
  (do
    (let [entity (map->input-entity i)
          new-input (insert-input<! entity)]
      (input-entity->map (assoc i :id (:id new-input)
                                :created_at (:created_at new-input)
                                :updated_at (:updated_at new-input))))))

(defn modify-input
  "Update input"
  [id i]
  (let [entity (map->input-entity (assoc i :id (java.util.UUID/fromString id)))
        updated-input (update-input<! entity)]
    (input-entity->map (assoc i :id (:id updated-input)
                              :created_at (:created_at updated-input)
                              :updated_at (:updated_at updated-input)))))

(defn delete-input
  "Delete input"
  [id]
  (delete-input! {:id (java.util.UUID/fromString id)}))

;;;;;;;;;;;;;;;;;;USER;;;;;;;;;;;;;;;;;;
(defn users
  []
  (map sanitize-user (find-all-users)))

(defn create-user
  "Adds a new user to the database.
   Returns the user with id."
  ([name email password]
   (create-user {:name name :email email :password password}))
  ([u]
   (log/debug "Inserting user" u)
   (let [user-info (assoc u :password (hashers/derive (:password u)))]
     (sanitize-user (create-user<! user-info)))))
