
(ns signal.predicate.identity
  (:require [signal.predicate.protocol :as proto]))

(def identifier :identity)

(defrecord IdentityClause [clause]
  proto/IPredicate
  (check [this value] true)
  (notification [this test-value]
    (str test-value " was within.")))

(defmethod proto/make-predicate identifier
  [pred]
  (->IdentityClause (:def pred)))
