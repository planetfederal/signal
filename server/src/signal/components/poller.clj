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

(ns signal.components.store.poller
  (:require [com.stuartsierra.component :as component]
            [signal.components.store.db :as storemodel]
            [signal.components.trigger.core :as triggerapi]
            [clj-http.client :as client]
            [cljts.io :as jtsio]
            [signal.specs.store]
            [overtone.at-at :refer [every, mk-pool, stop, stop-and-reset-pool!]]
            [clojure.tools.logging :as log])
  (:import (com.boundlessgeo.spatialconnect.schema SCCommand)))

(defn feature-collection->geoms
  "Given a geojson feature collection, return a list of the features' geometries"
  [fc]
  (if-let [features (.features fc)]
    (if (.hasNext features)
      (loop [feature (-> features .next .getDefaultGeometry)
             result []]
        (if (.hasNext features)
          (recur (-> features .next .getDefaultGeometry) (conj result feature))
          result))
      [])
    []))

(defn fetch-url
  "Fetches geojson from url and tests each feature for the specified trigger"
  [trigger url]
  (log/debug "Fetching" url)
  (let [res (client/get url)
        status (:status res)
        body (:body res)]
    (if (= status 200)
      (let [geoms (-> body
                      jtsio/read-feature-collection
                      feature-collection->geoms)]
        (doall (map (fn [g]
                      (triggerapi/test-value trigger "STORE" g)) geoms)))
      (log/error "Error Fetching" url))))

(def polling-stores (ref {}))
(def sched-pool (mk-pool))

(defn start-polling [trigger store]
  (let [seconds (get-in store [:options :polling])]
    (every (* 1000 (Integer/parseInt seconds))
           #(fetch-url trigger (:uri store)) sched-pool
           :job (keyword (:id store)) :initial-delay 5000)))

(defn stop-polling [store]
  (stop (keyword (:id store))))

(defn add-polling-store [trigger s]
  (if (not-empty (get-in s [:options :polling]))
    (do
      (dosync
       (commute polling-stores assoc (keyword (:id s)) s))
      (start-polling trigger s))))

(defn remove-polling-store [id]
  ; takes a store id string
  (dosync
   (commute polling-stores dissoc (keyword id)))
  (stop-polling (keyword id)))

(defn load-polling-stores [trigger]
  (doall (map (partial add-polling-store trigger) (storemodel/all))))

(defn create [store-comp s]
  (let [new-store (storemodel/create s)]
    (add-polling-store (:trigger store-comp) new-store)
    new-store))

(defn modify [store-comp id s]
  (let [updated-store (storemodel/modify id s)]

    (add-polling-store (:trigger store-comp) updated-store)
    updated-store))

(defn find-by-id [store-comp id]
  (storemodel/find-by-id id))

(defn delete [store-comp id]
  (storemodel/delete id)
  (remove-polling-store id))

(defn all [store-comp]
  (storemodel/all))

(defrecord StoreComponent [trigger]
  component/Lifecycle
  (start [this]
    (log/debug "Starting Store Component")
    (load-polling-stores trigger)
    (assoc this :trigger trigger))
  (stop [this]
    (log/debug "Stopping Store Component")
    this))

(defn make-store-component []
  (map->StoreComponent {}))
