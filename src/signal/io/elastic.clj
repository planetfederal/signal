(ns signal.io.elastic
  (:require [signal.io.protocol :as io-proto]
            [clojure.tools.logging :as log]))

(def identifier "elastic")

(defn send! [id message]
  (prn identifier id message))

(defrecord Elastic [id url]
  io-proto/Output
  (recipients [this]
    (:recipientes this))
  (send! [this message]
    (send! this message)))

(defmethod io-proto/make-output identifier
  [cfg]
  (map->Elastic cfg))
