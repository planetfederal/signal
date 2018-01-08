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

(ns signal.components.processor
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [signal.components.database :as db]
            [signal.components.notification :as notificationapi]
            [signal.output.email]
            [signal.output.protocol :as proto-output]
            [signal.output.webhook]
            [signal.predicate.geowithin]
            [signal.predicate.geodisjoint]
            [signal.predicate.protocol :as proto-pred]
            [yesql.core :refer [defqueries]])
  (:import [java.util Date]))

(def falsey-processors
  "processors that don't evaluate to true"
  (ref {}))

(def truthy-processors
  "If it's a valid processor, data has satisified the rules and we need to
  send an alert"
  (ref {}))

(defn- set-truthy-processor
  "Puts processor in valid-processors ref and removes it from invalid-processors ref"
  [processor]
  (dosync
   (commute falsey-processors dissoc (keyword (:id processor)))
   (commute truthy-processors assoc (keyword (:id processor)) processor)))

(defn- set-falsey-processor
  "Puts processor in invalid-processors ref and removes it from valid-processors ref"
  [processor]
  (dosync
   (commute falsey-processors assoc (keyword (:id processor)) processor)
   (commute truthy-processors dissoc (keyword (:id processor)))))

(defn- handle-success
  "Sets processor as valid, then sends a noification"
  [value processor notify]
  (let [geom-map (xy.geojson/write value)
        body (doall (->> (get-in processor [:definition :predicates])
                         (map #(proto-pred/notification % geom-map))
                         (clojure.string/join ",\n and ")))
        payload {:time  (str (Date.))
                 :processor (db/processor-by-id (:id processor))
                 :value geom-map ; Value that caused the alert to be true
                 :title (str "Alert from " (:name processor))
                 :body  (str geom-map "\n\n" body ".")}]
    (do
      (log/debug "Successful processing for:" (:id processor))
      (notificationapi/notify
       notify
       processor
       payload)
      (if-not (:repeated processor)
        (do
          (log/info "Removing processor " (:name processor) " with id:" (:id processor))
          (db/delete-processor (:id processor)))
        (set-truthy-processor processor)))))

(defn- handle-failure
  "Makes the processor invalid b/c it failed the test value."
  [processor]
  (if (nil? ((keyword (:id processor)) @truthy-processors))
    (set-falsey-processor processor)))

(defn- check-predicates
  "Maps over all invalid processors to check if they evaluate to true based
  on the value to test against all rules for a processor."
  [processor-comp value]
  (doall (map (fn [k]
                (if-let [processor (k @falsey-processors)]
                  (loop [preds (get-in processor [:definition :predicates])]
                    (if (empty? preds)
                      (handle-success value processor (:notify processor-comp))
                      (if-let [pred (first preds)]
                        (if (proto-pred/check pred value)
                          (recur (rest preds))
                          (handle-failure processor))))))) (keys @falsey-processors))))

(defn test-value
  "Posts a value to be checked on the source channel"
  [processor-comp value]
  ;; the source-channel is the source of incoming data
  ;; the store it came from
  ;; the value to be checked must be a geojson map with JTS :geometry
  (if-not (nil? value)
    (do (log/debug "Testing value:" value)
        (check-predicates processor-comp value))))

(defn add-processor
  "Adds a processor to the invalid-processors ref"
  [processor-comp processor]
  (log/debug "Adding processor" (:name processor))
  ;; builds a compound where clause of (rule AND rule AND ...)
  (let [output (proto-output/make-output (get-in processor [:definition :output]))
        predicates (doall (map proto-pred/make-predicate (get-in processor [:definition :predicates])))
        proc (-> (assoc-in processor [:definition :output] output)
                 (assoc-in [:definition :predicates] predicates))]
    (dosync
     (commute falsey-processors assoc (keyword (:id proc)) proc))))

(defn- evict-processor
  "Removes processor from both valid-processors and invalid-processors ref"
  [processor-comp processor]
  (log/trace "Removing processor" processor)
  (dosync
   (commute falsey-processors dissoc (keyword (:id processor)))
   (commute truthy-processors dissoc (keyword (:id processor)))))

(defn- load-processors
  "Fetches all processors from db and loads them into memory"
  [processor-comp]
  (let [processors (db/processors)]
    (map (partial add-processor processor-comp) processors)))

(defn all
  [_]
  (db/processors))

(defn find-by-id
  [_ id]
  (db/processor-by-id id))

(defn create
  [processor-comp t]
  (let [processor (db/create-processor t)]
    (add-processor processor-comp processor)
    processor))

(defn modify
  [processor-comp id t]
  (let [processor (db/modify-processor id t)]
    (add-processor processor-comp processor)
    processor))

(defn delete
  [processor-comp id]
  (db/delete-processor id)
  (evict-processor processor-comp id))

(defrecord ProcessorComponent [notify]
  component/Lifecycle
  (start [this]
    (log/debug "Starting Processor Component")
    (let [comp (assoc this :notify notify)]
      (doall (load-processors comp))
      comp))
  (stop [this]
    (log/debug "Stopping Processor Component")
    ()
    this))

(defn make-processor-component []
  (map->ProcessorComponent {}))

