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

(ns signal.components.notification
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan <!! >!! close! go alt!]]
            [postal.core :refer [send-message]]
            [signal.components.db :as db]
            [clojure.tools.logging :as log]
            [signal.output.protocol :as proto]))

(defn notify [_ processor payload]
  (let [recipients (proto/recipients (:output processor))
        ids (map :id
                 (db/create-notifications recipients "processor" payload))]
    (proto/send! (:output processor) (assoc payload :notif-ids ids))))

(defn find-notif-by-id
  [notif-comp id]
  (db/find-notif-by-id id))

(defrecord NotificationComponent []
  component/Lifecycle
  (start [this]
    (log/debug "Starting Notification Component")
    this)
  (stop [this]
    (log/debug "Stopping Notification Component")
    this))

(defn make-signal-notification-component []
  (->NotificationComponent))
