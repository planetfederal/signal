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
            [yesql.core :refer [defqueries]]
            [clojure.spec :as s]
            [signal.components.db :as db]
            [signal.components.notification :as notificationapi]
            [clojure.core.async :as async]
            [signal.entity.notification
             :refer [make-mobile-notification, make-email-notification]]
            [cljts.io :as jtsio]
            [signal.predicate.protocol :as proto-pred]
            [clojure.data.json :as json]
            [signal.processor.definition.geo
             :refer [make-within-clause]]
            [clojure.tools.logging :as log]))


(def falsey-processors
  "processors that don't evaluate to true"
  (ref {}))
(def truthy-processors
  "If it's a valid processor, data has satisified the rules and we need to
  send an alert"
  (ref {}))

(defn- add-processor
  "Adds a processor to the invalid-processors ref"
  [processor]
  (log/trace "Adding processor" processor)
  ;; builds a compound where clause of (rule AND rule AND ...)
  (let [t (assoc processor :predicates
                 (map proto-pred/make-predicate
                      (:rules processor)))]
    (dosync
     (commute falsey-processors assoc
              (keyword (:id t)) t))))

(defn- evict-processor
  "Removes processor from both valid-processors and invalid-processors ref"
  [processor]
  (log/trace "Removing processor" processor)
  (dosync
   (commute falsey-processors dissoc (keyword (:id processor)))
   (commute truthy-processors dissoc (keyword (:id processor)))))

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

(defn- load-processors
  "Fetches all processors from db and loads them into memory"
  []
  (let [processors (doall (db/processors))]
    (doall (map add-processor processors))))

(defn- handle-success
  "Sets processor as valid, then sends a noification"
  [value processor notify]
  (let [body    (doall (map #(proto-pred/notification % value) (:rules processor)))
        emails  (get-in processor [:recipients :emails])
        devices (get-in processor [:recipients :devices])
        processor (db/processor-by-id (:id processor))
        payload {:time    (str (new java.util.Date))
                 :value   (json/read-str (jtsio/write-geojson value))
                 :processor processor}]
    (do
      (if (some? devices)
        (notificationapi/notify
         notify
         (make-mobile-notification
          {:to       devices
           :priority "alert"
           :title    (str "Alert for processor: " (:name processor))
           :body     body
           :payload  payload})
         "processor"
         payload))
      (if (some? emails)
        (notificationapi/notify
         notify
         (make-email-notification
          {:to       emails
           :priority "alert"
           :title    (str "Alert for processor: " (:name processor))
           :body     body
           :payload  payload})
         "processor"
         payload))
      (if-not (:repeated processor)
        (do
          (log/info "Removing processor " (:name processor) " with id:" (:id processor))
          (db/delete-processor (:id processor)))))))

(defn- handle-failure
  "Makes the processor invalid b/c it failed the test value."
  [processor]
  (if (nil? ((keyword (:id processor)) @truthy-processors))
    (set-falsey-processor processor)))


(defn check-predicates
  "Maps over all invalid processors to check if they evaluate to true based
  on the value to test against all rules for a processor."
  [value notify]
  (doall (map (fn [k]
                (if-let [processor (k @falsey-processors)]
                  (loop [preds (:predicates processor)]
                    (if (empty? preds)
                      (handle-success value processor notify)
                      (if-let [pred (first preds)]
                        (if (proto-pred/check pred value)
                          (recur (rest preds))
                          (handle-failure processor))))))))))

(defn process-channel
  "Waits for input on channel to check values against processors"
  [notify input-channel]
  (async/go (while true
              (let [v (async/<! input-channel)
                    pt (:value v)]  ;; todo: support data types other than points
                (do (process-value (:store v) pt notify))))))

(defn test-value
  "Posts a value to be checked on the source channel"
  [processorcomp store value]
  ;; the source-channel is the source of incoming data
    ;; the store it came from
  ;; the value to be checked
  (if-not (or (nil? store) (nil? value))
    (async/go (async/>! (:source-channel processorcomp)
                        {:store store :value value}))
    (log/error "Store and value must be set")))

(defn all
  [processor-comp]
  (db/processors))

(defn find-by-id
  [processor-comp id]
  (db/processor-by-id id))

(defn create
  [processor-comp t]
  (let [processor (db/create-processor t)]
    (add-processor processor)
    processor))

(defn modify
  [processor-comp id t]
  (let [processor (db/modify-processor id t)]
    (add-processor processor)
    processor))

(defn delete
  [processor-comp id]
  (db/delete-processor id)
  (evict-processor id))

(defrecord ProcessorComponent [notify]
  component/Lifecycle
  (start [this]
    (log/debug "Starting processor Component")
    (let [c (async/chan)
          comp (assoc this :source-channel c :notify notify)]
      (process-channel (:notify comp) (:source-channel comp))
      (load-processors)
      comp))
  (stop [this]
    (log/debug "Stopping processor Component")
    (async/close! (:source-channel this))
    this))

(defn make-processor-component []
  (map->ProcessorComponent {}))

