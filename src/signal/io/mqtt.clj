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

(ns signal.io.mqtt
  (:require [signal.io.protocol :as io-proto]
            [clojurewerkz.machine-head.client :as mh]
            [clojure.tools.logging :as log]
            [signal.config :refer [config]]
            [xy.geojson :as geojson])
  (:import (org.eclipse.paho.client.mqttv3 MqttException)))

(def identifier "mqtt")

(def connection (atom nil))

(defn- connect
  "Connects to mqtt broker"
  ([id url port]
   (while (or (nil? @connection) (not (mh/connected? @connection)))
     (let [uri (str url ":" (str port))]
       (log/debug (str "Connecting MQTT Client to " uri))
       (try (reset! connection (mh/connect uri id))
            (catch MqttException e
              (Thread/sleep 1000)))))))

(defn receive
  [f ^String topic _ ^bytes payload]
  (log/debugf "Received message on %s" topic)
  (-> (String. payload "UTF-8")
      geojson/str->map
      f))

(defn subscribe
  "Subscribe to mqtt topic with message handler function f"
  ([id url port topic cb]
   (if (or (nil? @connection) (not (mh/connected? @connection)))
     (let []
       (do (connect id url port))))
   (log/debugf "Subscribing to topic %s" topic)
   (mh/subscribe @connection {topic 2} (partial receive cb))))

(defrecord MqttConsumer [id url port topic]
  io-proto/StreamingInput
  (start-input [this func]
    (subscribe id url port topic func)
    this)
  (stop-input [this]
    (mh/disconnect @connection)
    this))

(defmethod io-proto/make-streaming-input identifier
  [cfg]
  (let [ident (get-in config [:input :mqtt :subscriber-id])
        url (get-in config [:input :mqtt :url])
        port (get-in config [:input :mqtt :port])
        topic (get-in cfg [:definition :topic])]
    (->MqttConsumer ident url port topic)))

(defrecord MqttProducer [id url port topic]
  io-proto/StreamingOutput
  (start-output [this func]
    (subscribe id url port topic func)
    this)
  (stop-output [this]
    (mh/disconnect @connection)
    this))

(defmethod io-proto/make-streaming-output identifier
  [cfg]
  (let [ident (get-in config [:input :mqtt :subscriber-id])
        url (get-in config [:input :mqtt :url])
        port (get-in config [:input :mqtt :port])
        topic (get-in cfg [:definition :topic])]
    (->MqttConsumer ident url port topic)))
