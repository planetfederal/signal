(ns signal.input.http
  (:require [signal.input.poll-proto :as proto]
            [clj-http.client :as http]))

(def identifier :http)

(defrecord Http [id url interval]
  proto/IPollingInput
  (interval [this] (if (some? (:interval this))
                     (:interval this)
                     0))
  (poll [this func]
    (try
      (let [resp (http/get (:url this))
            json (cheshire.core/parse-string (:body resp))]
        (func json))
      (catch Exception e (.getLocalizedMessage e)))))

(defmethod proto/make-polling-input identifier
  [cfg]
  (map->Http cfg))