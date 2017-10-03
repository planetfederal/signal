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
            [signal.input.poll-proto :as poll-proto]
            [signal.input.stream-proto :as stream-proto]
            [cljts.io :as jtsio]
            [overtone.at-at :refer [every, mk-pool, stop, stop-and-reset-pool!]]
            [clojure.tools.logging :as log]))

(def inputs (ref {}))
(def sched-polling-pool (mk-pool))

(defn fetch-url
  [polling-input]
  (poll-proto/poll polling-input (:fn polling-input)))

(defn start-polling [polling-input]
  (let [seconds (:interval polling-input)]
    (every (* 1000 seconds)
           #(fetch-url polling-input) sched-polling-pool
           :job (keyword (:id polling-input)) :initial-delay 5000)))

(defn start-streaming
  [streaming-input func]
  (stream-proto/start streaming-input))

(defn stop-polling [processor]
  (stop (keyword (:id processor))))

(defn stop-streaming
  [streaming-input]
  (stream-proto/stop streaming-input))

(defn add-streaming-input [streaming-input func]
  (start-streaming streaming-input func)
  (commute inputs assoc (keyword (:id streaming-input)) (assoc streaming-input :fn func)))

(defn add-polling-input [polling-input func]
  (if (< 0 (:interval polling-input))
    (let [input-fn (assoc polling-input :fn func)]
      (dosync
        (commute inputs assoc (keyword (:id polling-input)) input-fn))
      (start-polling input-fn))))

(defn remove-streaming-input [streaming-input]
  (stop-streaming streaming-input)
  (commute inputs dissoc (keyword (:id streaming-input))))

(defn remove-polling-input [_ polling-input]
  ; takes a store id string
  (dosync
    (commute inputs dissoc (keyword (:id polling-input))))
  (stop-polling (keyword (:id polling-input))))

(defn add-input [poller-comp input func]
  (if (satisfies? stream-proto/IStreamingInput input)
    (add-streaming-input input func)
    (add-polling-input input func)))

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
