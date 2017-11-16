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
            [clj-http.client :as client]
            [signal.components.processor :as processor-api]
            [signal.input.poll-proto :as poll-proto]
            [signal.input.stream-proto :as stream-proto]
            [signal.input.mqtt]
            [overtone.at-at :refer [every, mk-pool, stop, stop-and-reset-pool!]]
            [clojure.tools.logging :as log]))

(def inputs (ref {}))
(def sched-polling-pool (mk-pool))

(defn fetch-url
  [polling-input]
  (poll-proto/poll polling-input (:fn polling-input)))

(defn- start-polling [polling-input]
  (let [seconds (:interval polling-input)]
    (every (* 1000 seconds)
           #(fetch-url polling-input) sched-polling-pool
           :job (keyword (:id polling-input)) :initial-delay 5000)))

(defn add-streaming-input [input-comp streaming-input]
  (let [proc (:processor input-comp)
        func (partial processor-api/test-value proc)
        input (stream-proto/make-streaming-input streaming-input func)]
    (dosync
     (stream-proto/start input)
     (commute inputs assoc (keyword (:id streaming-input)) (assoc streaming-input :fn func)))))

(defn add-polling-input [input-comp input]
  (let [proc (:processor input-comp)
        func (partial processor-api/test-value proc)
        polling-input (poll-proto/make-polling-input input)]
    (if (< 0 (:interval polling-input))
      (let [input-fn (assoc polling-input :fn func)]
        (dosync
         (commute inputs assoc (keyword (:id polling-input)) input-fn)
         (start-polling input-fn))))))

(defn remove-streaming-input [streaming-input]
  (stream-proto/stop streaming-input)
  (commute inputs dissoc (keyword (:id streaming-input))))

(defn remove-polling-input [_ polling-input]
  ; takes a store id string
  (dosync
   (commute inputs dissoc (keyword (:id polling-input)))
   (stop (keyword (:id polling-input)) sched-polling-pool)))

(defrecord InputManagerComponent [processor]
  component/Lifecycle
  (start [this]
    (log/debug "Starting Store Component")
    (assoc this :processor processor))
  (stop [this]
    (log/debug "Stopping Store Component")
    this))

(defn make-input-manager-component []
  (map->InputManagerComponent {}))
