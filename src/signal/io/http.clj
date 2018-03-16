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

(ns signal.io.http
  (:require [clj-http.client :as http]
            [xy.geojson :as geojson]
            [signal.input.poll-proto :as proto]
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
