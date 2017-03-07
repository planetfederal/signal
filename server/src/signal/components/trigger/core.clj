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

(ns signal.components.trigger.core
  (:require [com.stuartsierra.component :as component]
            [yesql.core :refer [defqueries]]
            [clojure.spec :as s]
            [signal.components.http.intercept :as intercept]
            [signal.components.http.response :as response]
            [signal.components.trigger.db :as triggermodel]
            [signal.components.notification.core :as notificationapi]
            [clojure.core.async :as async]
            [signal.entity.notification
             :refer [make-mobile-notification, make-email-notification]]
            [cljts.io :as jtsio]
            [signal.trigger.protocol :as proto-clause]
            [clojure.data.json :as json]
            [signal.trigger.definition.geo
             :refer [make-within-clause]]
            [clojure.tools.logging :as log]
            [signal.components.form.db :as formmodel]))

(def falsey-triggers
  "Triggers that don't evaluate to true"
  (ref {}))
(def truthy-triggers
  "If it's a valid trigger, data has satisified the rules and we need to
  send an alert"
  (ref {}))

(defn- add-trigger
  "Adds a trigger to the invalid-triggers ref"
  [trigger]
  (log/trace "Adding trigger" trigger)
  ;; builds a compound where clause of (rule AND rule AND ...)
  (let [t (assoc trigger :rules
                 (map (fn [rule]
                        (case (:comparator rule)
                          ;; todo: implement other spatial relations
                          "$geowithin" (make-within-clause
                                        (:id trigger) (:rhs rule))
                          nil))
                      (:rules trigger)))]
    (dosync
     (commute falsey-triggers assoc
              (keyword (:id t)) t))))

(defn- evict-trigger
  "Removes trigger from both valid-triggers and invalid-triggers ref"
  [trigger]
  (log/trace "Removing trigger" trigger)
  (dosync
   (commute falsey-triggers dissoc (keyword (:id trigger)))
   (commute truthy-triggers dissoc (keyword (:id trigger)))))

(defn- set-truthy-trigger
  "Puts trigger in valid-triggers ref and removes it from invalid-triggers ref"
  [trigger]
  (dosync
   (commute falsey-triggers dissoc (keyword (:id trigger)))
   (commute truthy-triggers assoc (keyword (:id trigger)) trigger)))

(defn- set-falsey-trigger
  "Puts trigger in invalid-triggers ref and removes it from valid-triggers ref"
  [trigger]
  (dosync
   (commute falsey-triggers assoc (keyword (:id trigger)) trigger)
   (commute truthy-triggers dissoc (keyword (:id trigger)))))

(defn- load-triggers
  "Fetches all triggers from db and loads them into memory"
  []
  (let [triggers (doall (triggermodel/all))]
    (doall (map add-trigger triggers))))

(defn- handle-success
  "Sets trigger as valid, then sends a noification"
  [value trigger notify]
  (let [body    (doall (map #(proto-clause/notification % value) (:rules trigger)))
        emails  (get-in trigger [:recipients :emails])
        devices (get-in trigger [:recipients :devices])
        trigger (triggermodel/find-by-id (:id trigger))
        payload {:time    (str (new java.util.Date))
                 :value   (json/read-str (jtsio/write-geojson value))
                 :trigger trigger}]
    (do
      (if (some? devices)
        (notificationapi/notify
         notify
         (make-mobile-notification
          {:to       devices
           :priority "alert"
           :title    (str "Alert for Trigger: " (:name trigger))
           :body     body
           :payload  payload})
         "trigger"
         payload))
      (if (some? emails)
        (notificationapi/notify
         notify
         (make-email-notification
          {:to       emails
           :priority "alert"
           :title    (str "Alert for Trigger: " (:name trigger))
           :body     body
           :payload  payload})
         "trigger"
         payload))
      (if-not (:repeated trigger)
        (do
          (log/info "Removing trigger " (:name trigger) " with id:" (:id trigger))
          (triggermodel/delete (:id trigger)))))))

(defn- handle-failure
  "Makes the trigger invalid b/c it failed the test value."
  [trigger]
  (if (nil? ((keyword (:id trigger)) @truthy-triggers))
    (set-falsey-trigger trigger)))

(defn process-value
  "Maps over all invalid triggers to check if they evaluate to true based
  on the value to test against all rules for a trigger."
  [store value notify]
  (doall
   (map (fn [k]
          (if-let [trigger (k @falsey-triggers)]
            (if-not (empty? (:rules trigger))
              (if (or
                   (= "location" store) ; TODO delete when location data store is completed
                   (empty? (:stores trigger))           ; empty stores means test all stores
                   (>= 0 (.indexOf (:stores trigger) store))) ; index found means to filter
                (loop [rules (:rules trigger)]
                  (if (empty? rules)
                     ; All the rules have passed, send a notification
                    (handle-success value trigger notify)
                    (if-let [rule (first rules)]
                      (if (proto-clause/check rule value)
                        (recur (rest rules))
                        (handle-failure trigger)))))))))
        (keys @falsey-triggers))))

(defn process-channel
  "Waits for input on channel to check values against triggers"
  [notify input-channel]
  (async/go (while true
              (let [v (async/<! input-channel)
                    pt (:value v)]  ;; todo: support data types other than points
                (do (process-value (:store v) pt notify))))))

(defn test-value
  "Posts a value to be checked on the source channel"
  [triggercomp store value]
  ;; the source-channel is the source of incoming data
    ;; the store it came from
  ;; the value to be checked
  (if-not (or (nil? store) (nil? value))
    (async/go (async/>! (:source-channel triggercomp)
                        {:store store :value value}))
    (log/error "Store and value must be set")))

(defn all
  [trigger-comp]
  (triggermodel/all))

(defn find-by-id
  [trigger-comp id]
  (triggermodel/find-by-id id))

(defn create
  [trigger-comp t]
  (let [trigger (triggermodel/create t)]
    (add-trigger trigger)
    trigger))

(defn modify
  [trigger-comp id t]
  (let [trigger (triggermodel/modify id t)]
    (add-trigger trigger)
    trigger))

(defn delete
  [trigger-comp id]
  (triggermodel/delete id)
  (evict-trigger id))

(defrecord TriggerComponent [notify]
  component/Lifecycle
  (start [this]
    (log/debug "Starting Trigger Component")
    (let [c (async/chan)
          comp (assoc this :source-channel c :notify notify)]
      (process-channel (:notify comp) (:source-channel comp))
      (load-triggers)
      comp))
  (stop [this]
    (log/debug "Stopping Trigger Component")
    (async/close! (:source-channel this))
    this))

(defn make-trigger-component []
  (map->TriggerComponent {}))

