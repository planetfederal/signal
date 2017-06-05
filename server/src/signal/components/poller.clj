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
            [signal.components.processor :as processorapi]
            [clj-http.client :as client]
            [cljts.io :as jtsio]
            [signal.specs.store]
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
  [processor func]
  (let [url (:url processor)]
    (if-let [res (client/get url)]
      (if (= (:status res) 200)
        (let [geoms (-> (:body res)
                        jtsio/read-feature-collection
                        feature-collection->geoms)]
          (doall (map #(processorapi/test-value processor %))))
        (log/error "Error fetching " url)))))


(def polling-stores (ref {}))
(def sched-pool (mk-pool))

(defn start-polling [processor]
  (let [seconds (get-in processor [:options :polling])]
    (every (* 1000 (Integer/parseInt seconds))
           #(fetch-url processor (:uri store)) sched-pool
           :job (keyword (:id store)) :initial-delay 5000)))

(defn stop-polling [processor]
  (stop (keyword (:id processor))))

(defn add-polling [polling-comp processor func]
  (if (not-empty (get-in s [:options :polling]))
    (do
      (dosync
       (commute polling-stores assoc (keyword (:id s)) s))
      (start-polling processor s))))

(defn remove-polling-store [polling-comp id]
  ; takes a store id string
  (dosync
   (commute polling-stores dissoc (keyword id)))
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
