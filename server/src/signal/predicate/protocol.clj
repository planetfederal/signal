(ns signal.predicate.protocol)

(defprotocol IPredicate
  (field-path [this])
  (predicate [this])
  (check [this value])
  (notification [this v]))

(defmulti make-predicate :type)