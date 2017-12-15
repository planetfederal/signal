(ns signal.input.http
  (:require [signal.input.poll-proto :as proto]
            [clj-http.client :as http]
            [xy.geojson :as geojson]
            [clojure.tools.logging :as log]))

(def identifier "http")

(defrecord Http [id url interval]
  proto/IPollingInput
  (interval [this] (if (some? (:interval this))
                     (:interval this)
                     60))
  (poll [this func]
    (try
      (log/debug "Polling:" (:id this) " " (:url this))
      (let [resp (http/get (:url this))
            json (geojson/str->map (:body resp))]
        (case (:type json)
          "FeatureCollection" (doseq [x (:features json)] (func x))
          "GeometryCollection" (doseq [x (:geometries json)] (func x))
          (func json)))
      (catch Exception e (log/error e (.getLocalizedMessage e))))))

(defmethod proto/make-polling-input identifier
  [cfg]
  (->Http (:id cfg)
          (get-in cfg [:definition :url])
          (get-in cfg [:definition :interval])))
