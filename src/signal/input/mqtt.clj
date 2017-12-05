(ns signal.input.mqtt
  (:require [signal.input.stream-proto :as proto]
            [clojurewerkz.machine-head.client :as mh]
            [clojure.tools.logging :as log]
            [xy.geojson :as geojson])
  (:import (org.eclipse.paho.client.mqttv3 MqttException)))

(def connection (atom nil))

(defn- connect
  "Connects to mqtt broker"
  ([id url port]
   (while (or (nil? @connection) (not (mh/connected? @connection)))
     (let [uri (str url ":" port)]
       (log/debug (str "Connecting MQTT Client to " uri))
       (try (reset! connection (mh/connect uri id))
            (catch MqttException e
              (Thread/sleep 1000)))))))

(defn receive
  [f ^String _ _ ^bytes payload]
  (-> (String. payload "UTF-8")
      geojson/str->map
      f))

(defn subscribe
  "Subscribe to mqtt topic with message handler function f"
  ([[id url port topic cb]]
   (subscribe id url port topic cb))
  ([id url port topic cb]
   (if (or (nil? @connection) (not (mh/connected? @connection)))
     (do (connect id url port)))
   (log/debugf "Subscribing to topic" topic)
   (mh/subscribe @connection {topic 2} (partial receive cb))))

(defrecord MqttConsumer [id url port topic cb]
  proto/IStreamingInput
  (start [_]
    (subscribe id url port topic cb))
  (stop [_]
    (mh/disconnect @connection)))

(defmethod proto/make-streaming-input "mqtt"
  [cfg cb-fn]
  (map->MqttConsumer (assoc cfg :cb cb-fn)))
