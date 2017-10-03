(ns signal.output.webhook
  (:require [signal.output.protocol :as proto]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]))

(def identifier :webhook)

(defrecord Webhook [url]
  proto/IOutput
  (recipients [this] [(:url this)])
  (send! [this v]
    (log/info (:url this))
    (http/post (:url this) {:body (json/generate-string v)
                            :content-type :json})))

(defmethod proto/make-output identifier
  [cfg]
  (map->Webhook cfg))