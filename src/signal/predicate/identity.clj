
(ns signal.predicate.identity
  (:require [signal.predicate.protocol :as proto]))

(def identifier "identity")

(defrecord IdentityClause [clause]
  proto/IPredicate
  (check [this value] true)
  (notification [this _]
    (str " was within identity" (:clause this))))

(defmethod proto/make-predicate identifier
  [pred]
  (->IdentityClause (:definition pred)))
