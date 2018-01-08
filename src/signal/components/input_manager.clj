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

(ns signal.components.input-manager
  (:require [com.stuartsierra.component :as component]
            [signal.components.processor :as processor-api]
            [signal.input.poll-proto :as poll-proto]
            [signal.input.stream-proto :as stream-proto]
            [signal.input.http]
            [signal.components.database :as database-api]
            [overtone.at-at :refer [every, mk-pool, stop, stop-and-reset-pool!]]
            [clojure.tools.logging :as log]))

(def inputs (ref {}))
(def schedule-polling-pool (mk-pool))

(defn fetch-url
  [polling-input]
  (try (poll-proto/poll polling-input (:fn polling-input))
       (catch Exception e
         (log/error e (.getLocalizedMessage e)))))

(defn all [_] (database-api/inputs))

(defn find-by-id [_ id] (database-api/input-by-id id))

(defn- start-polling
  "Returns at-at job with metadata"
  [polling-input]
  (let [seconds (poll-proto/interval polling-input)]
    (every (* 1000 seconds)
           #(fetch-url polling-input)
           schedule-polling-pool
           :initial-delay 5000)))

(defn add-streaming-input [input-comp streaming-input]
  (let [processor (:processor input-comp)
        func (partial processor-api/test-value processor)
        input (stream-proto/make-streaming-input streaming-input func)]
    (dosync
     (stream-proto/start input)
     (commute inputs assoc (keyword (:id streaming-input)) (assoc streaming-input :fn func)))))

(defn add-polling-input [input-comp input]
  (let [processor (:processor input-comp)
        func (partial processor-api/test-value processor)
        polling-input (poll-proto/make-polling-input input)]
    (if (< 0 (poll-proto/interval polling-input))
      (let [input-fn (assoc polling-input :fn func)
            job (start-polling input-fn)]
        (dosync
         (commute inputs assoc (keyword (:id polling-input)) {:fn input-fn :job-id (:id job)}))))))

(defn remove-streaming-input [streaming-input]
  (stream-proto/stop streaming-input)
  (commute inputs dissoc (keyword (:id streaming-input))))

(defn stop-input
  [_ id]
  (let [input (get @inputs (keyword id))
        job-id (:job-id input)]
    (log/debug "Stopping Job:" job-id)
    (if (stop job-id schedule-polling-pool)
      (dosync
       (commute inputs dissoc (keyword id))
       true)
      false)))

(defn remove-polling-input!
  "Removes input from the scheduler pool, inputs ref, and database"
  [_ id]
  (if (stop-input _ id)
    (database-api/delete-input id)
    false))

(defn- start-inputs
  "Fetches all inputs from the database and starts them"
  [input-comp]
  (doall (map (partial add-polling-input input-comp) (database-api/inputs))))

(defn- stop-inputs
  [input-comp]
  (doall (map (fn [i]
                (stop-input input-comp (:id i)))
              (database-api/inputs))))

(defn create
  "Creates and input and adds it to the scheduler"
  [input-comp i]
  (let [input (database-api/create-input i)]
    (add-polling-input input-comp input)
    input))

(defn modify
  "Updates an existing input"
  [input-comp id i]
  (let [input (database-api/modify-input id i)]
    (add-polling-input input-comp input)))

(defn delete
  "Return true if successfully deleted"
  [input-comp id]
  (do
    (log/debug "Removing input:" id)
    (remove-polling-input! input-comp id)))

(defrecord InputManagerComponent [processor]
  component/Lifecycle
  (start [this]
    (log/debug "Starting Store Component")
    (let [cmp (assoc this :processor processor)]
      (do (start-inputs cmp)
          cmp)))
  (stop [this]
    (log/debug "Stopping Store Component")
    (do (stop-inputs this)
        this)))

(defn make-input-manager-component []
  (map->InputManagerComponent {}))
