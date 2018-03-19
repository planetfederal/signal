;; Copyright 2016-2018 Boundless, http://boundlessgeo.com
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

(ns signal.io.email
  (:require [signal.io.protocol :as proto]
            [postal.core :as postal]
            [signal.config :as config]
            [signal.components.database :as db]))

(def identifier "email")

(def conn {:host (get-in config/config [:output :email :smtp-host])
           :ssl  true
           :user (get-in config/config [:output :email :smtp-user])
           :pass (get-in config/config [:output :email :smtp-password])})

(defn- build-notification-link
  [id]
  (let [hostname (or (get-in config/config [:app :hostname])
                     (.getHostName (java.net.InetAddress/getLocalHost)))]
    (str "http://" hostname "/notifications/" id)))

(defn email-recipient
  [recipient message]
  (postal/send-message conn {:from    "mobile@boundlessgeo.com"
                             :to      (str recipient)
                             :subject (str (:title message))
                             :body    (str (:body message))}))

(defn- send!
  [email-output message]
  (let [recipients (do (zipmap (:notif-ids message) (:recipients email-output)))]
    (doall (map (fn [[id recipient]]
                  (email-recipient recipient (assoc message :body (str (build-notification-link id)
                                                                       "\n\n"
                                                                       (:body message))))
                  (db/mark-as-sent id))
                recipients))))

(defrecord Email [recipients]
  proto/Output
  (recipients [this]
    (:recipients this))
  (send! [this message]
    (send! this message)))

(defmethod proto/make-output identifier
  [cfg]
  (map->Email cfg))
