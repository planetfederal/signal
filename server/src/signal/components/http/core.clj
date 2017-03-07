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

(ns signal.components.http.core
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [com.stuartsierra.component :as component]
   [signal.components.http.auth :as auth]
   [signal.components.http.form :as form-http]
   [signal.components.http.config :as config-http]
   [signal.components.http.device :as device-http]
   [signal.components.http.location :as location-http]
   [signal.components.http.mqtt :as mqtt-http]
   [signal.components.http.notification :as notif-http]
   [signal.components.http.store :as store-http]
   [signal.components.http.ping :as ping-http]
   [signal.components.http.user :as user-http]
   [signal.components.http.trigger :as trigger-http]
   [signal.components.http.team :as team-http]
   [clojure.tools.logging :as log]))

(defrecord HttpService [http-config ping user team device location trigger store config form mqtt notify]
  component/Lifecycle
  (start [this]
    (log/debug "Starting HttpService component")
    (let [routes #(route/expand-routes
                   (clojure.set/union #{}
                                      (auth/routes)
                                      (ping-http/routes ping)
                                      (user-http/routes user)
                                      (team-http/routes team)
                                      (device-http/routes device)
                                      (location-http/routes location)
                                      (trigger-http/routes trigger)
                                      (store-http/routes mqtt store)
                                      (config-http/routes config)
                                      (mqtt-http/routes mqtt)
                                      (form-http/routes form mqtt)
                                      (notif-http/routes notify)))]
      (assoc this :service-def (merge http-config
                                      {:env                     :prod
                                       ::http/routes            routes
                                       ::http/resource-path     "/public"
                                       ::http/type              :jetty
                                       ::http/port              (or (some-> (System/getenv "PORT")
                                                                            Integer/parseInt)
                                                                    8085)
                                       ::http/allowed-origins   {:creds           true
                                                                 :allowed-origins (constantly true)}
                                       ::http/container-options {:h2c? true
                                                                 :h2?  false
                                                                 :ssl? false}}))))

  (stop [this]
    (log/debug "Starting HttpService component")
    this))

(defrecord SignalHttpService [http-config user team notify trigger store]
  component/Lifecycle
  (start [this]
    (log/debug "Starting SignalHttpService")
    (let [routes #(route/expand-routes
                   (clojure.set/union #{}
                                      (auth/routes)
                                      (user-http/routes user)
                                      (team-http/routes team)
                                      (notif-http/routes notify)
                                      (trigger-http/routes trigger)
                                      (store-http/routes nil store)))]
      (assoc this :service-def (merge http-config
                                      {:env                     :prod
                                       ::http/routes            routes
                                       ::http/resource-path     "/public"
                                       ::http/type              :jetty
                                       ::http/port              (or (some-> (System/getenv "PORT")
                                                                            Integer/parseInt)
                                                                    8085)
                                       ::http/allowed-origins   {:creds           true
                                                                 :allowed-origins (constantly true)}
                                       ::http/container-options {:h2c? true
                                                                 :h2?  false
                                                                 :ssl? false}}))))

  (stop [this]
    (log/debug "Stopping SignalHttpService")
    this))

(defn make-http-service-component [http-config]
  (map->HttpService {:http-config http-config}))

(defn make-signal-http-service-component [http-config]
  (map->SignalHttpService {:http-config http-config}))
