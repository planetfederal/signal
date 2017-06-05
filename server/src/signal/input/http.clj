(ns signal.input.http
  (:require [signal.input.protocol :as proto]))

(def identifier "http")
(def polling? true)

(defrecord Http [url interval]
  proto/IInput
  (interval [this] (if (some? (:interval this))
                     (:interval this)
                     0))
  (recv [this value]))

(defmethod proto/make-input identifier
  [cfg]
  (map->Http cfg))