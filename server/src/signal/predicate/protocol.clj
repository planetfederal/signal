(ns signal.predicate.protocol)

(defprotocol IPredicate
  (check [this value])
  (notification [this v]))

(defmulti make-predicate :type)
