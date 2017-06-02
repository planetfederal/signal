(ns signal.filter.identity
  (:require [signal.filter.protocol :as proto]))

(def identifier "identity")

(defrecord IdentityFilter []
  proto/IFilter
  (exec [this v] true))

(defmethod proto/make-filter identifier
  [cfg]
  (map->IdentityFilter {}))
