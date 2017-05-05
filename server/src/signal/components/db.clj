(ns signal.components.db
  (:require [signal.db.conn :as db]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [buddy.hashers :as hashers]
            [clojure.java.jdbc :as jdbc]))

;;;;;;;;;;;;;;;;;SQL;;;;;;;;;;;;;;
(defqueries "sql/notifications.sql" {:connection db/db-spec})
(defqueries "sql/user.sql" {:connection db/db-spec})

;;;;;;;;;;;SANITIZERS;;;;;;;;
(defn sanitize-timestamps [v]
  (dissoc n :updated_at :deleted_at))

(defn sanitize-user [u]
  (dissoc (sanitize-timestamps u) :password :created_at))

;;;;;;;;;;;;UTILS;;;;;;;;;;;;;;;;;;
(deftype StringArray [items]
  clojure.java.jdbc/ISQLParameter
  (set-parameter [_ stmt ix]
    (let [as-array (into-array Object items)
          jdbc-array (.createArrayOf (.getConnection stmt) "text" as-array)]
      (.setArray stmt ix jdbc-array))))

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
  jdbc/IResultSetReadColumn
  (result-set-read-column [val rsmeta idx]
    (let [colType (.getColumnTypeName rsmeta idx)]
      (if (contains? #{"json" "jsonb"} colType)
        (json/read-str (.getValue val) :key-fn clojure.core/keyword)
        val))))

;;;;;;;;;;;;NOTIFICATION;;;;;;;;;;;
(defn- create-message [message-type info]
  (insert-message<!
    {:type message-type :info (json/write-str info)}))

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

;;;;;;;;;;USERS;;;;;;;;;;;
(defn users
  []
  (map sanitize-user (find-all-users)))

(defn create-user
  "Adds a new user to the database.
   Returns the user with id."
  [u]
  (log/debug "Inserting user" u)
  (let [user-info {:name     (:name u)
                   :email    (:email u)
                   :password (hashers/derive (:password u))}]
    (sanitize-user (create-user<! user-info))))
