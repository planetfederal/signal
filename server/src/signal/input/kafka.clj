(ns signal.input.kafka
  (:require [signal.input.stream-proto :as proto]))

(def identifier :kafka)

(defrecord KafkaConsumer [id]
  proto/IStreamingInput
  (start [this])
  (stop [this])
  (recv [this value]))

(defmethod proto/make-streaming-input identifier
  [cfg]

  (map->KafkaConsumer cfg))
