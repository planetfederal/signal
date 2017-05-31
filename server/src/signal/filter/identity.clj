(ns signal.filter.identity
  (:require [signal.filter.protocol :as proto]))

(defrecord IdentityFilter []
  proto/IFilter
  (exec [this v] true))

(defmethod proto/make-filter :identity
  []
  (map->IdentityFilter {}))
