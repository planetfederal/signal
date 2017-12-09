(ns signal.reducer.identity
  (:require [signal.reducer.protocol :as proto]))

(def identifier "identity")

(defrecord IdentityReducer [cfg]
  proto/IReducer
  (exec [this v] (identity v)))

(defmethod proto/make-reducer identifier
  [cfg]
  (->IdentityReducer cfg))
