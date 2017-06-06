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

(ns signal.components.poller
  (:require [com.stuartsierra.component :as component]
            [clj-http.client :as client]
            [cljts.io :as jtsio]
            [overtone.at-at :refer [every, mk-pool, stop, stop-and-reset-pool!]]
            [clojure.tools.logging :as log]))

(defn feature-collection->geoms
  "Given a geojson feature collection, return a list of the features' geometries"
  [fc]
  (if-let [features (.features fc)]
    (if (.hasNext features)
      (loop [feature (-> features .next .getDefaultGeometry)
             result []]
        (if (.hasNext features)
          (recur (-> features .next .getDefaultGeometry) (conj result feature))
          result)) []) []))

(defn fetch-url
  [input]
  (let [url (:url input)]
    (if-let [res (client/get url)]
      (if (= (:status res) 200)
        (let [geoms (-> (:body res)
                        jtsio/read-feature-collection
                        feature-collection->geoms)]
          (doall (map #((:fn input) %))) geoms)
        (log/error "Error fetching " url)))))

(def polling-inputs (ref {}))
(def sched-pool (mk-pool))

(defn start-polling [input]
  (let [seconds (:interval input)]
    (every (* 1000 seconds)
           #(fetch-url input) sched-pool
           :job (keyword (:processor-id input)) :initial-delay 5000)))

(defn stop-polling [processor]
  (stop (keyword (:id processor))))

(defn add-polling-input [poller-comp processor func]
  (let [input (:input processor)]
    (if (< 0 (:interval input))
      (do
        (let [input-fn (assoc input :fn func :processor-id (:id processor))]
          (dosync
            (commute polling-inputs assoc
                     (keyword (:id processor)) input-fn))
          (start-polling input-fn))))))

(defn remove-polling-input [_ id]
  ; takes a store id string
  (dosync
   (commute polling-inputs dissoc (keyword id)))
  (stop-polling (keyword id)))

(defrecord PollingManagementComponent
  []
  component/Lifecycle
  (start [this]
    (log/debug "Starting Store Component")
    this)
  (stop [this]
    (log/debug "Stopping Store Component")
    this))

(defn make-polling-component []
  (map->PollingManagementComponent {}))
