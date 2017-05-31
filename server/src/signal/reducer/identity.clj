(ns signal.reducer.identity
  (:require [signal.reducer.protocol :as proto]))

(defrecord IdentityReducer [trigger-id cfg]
  proto/IReducer
  (exec [this v] (identity v)))

(defmethod proto/make-reducer :identity
  [trigger-id cfg]
  (->IdentityReducer))
