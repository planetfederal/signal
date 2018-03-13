(ns signal.input.kafka
  (:require [signal.input.stream-proto :as proto]))

(def identifier "kafka")

(defrecord KafkaConsumer [id]
  proto/IStreamingInput
  (start [this func])
  (stop [this]))

(defmethod proto/make-streaming-input identifier
  [cfg]
  (map->KafkaConsumer cfg))
