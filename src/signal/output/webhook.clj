(ns signal.output.webhook
  (:require [signal.output.protocol :as proto]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [signal.components.database :as db]))

(def identifier "webhook")

(defn- send!
  [webhook value]
  (log/info (:url webhook))
  (doall (map (fn [id]
                (do
                  (http/post (:url webhook) {:body (json/generate-string value)
                                             :content-type :json})
                  (db/mark-as-sent id)) (:notif-ids value)))))

(defrecord Webhook [url verb]
  proto/IOutput
  (recipients [this] [(:url this)])
  (send! [this value]
    (send! this value)))

(defmethod proto/make-output identifier
  [cfg]
  (map->Webhook cfg))
