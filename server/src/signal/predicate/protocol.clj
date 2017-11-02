(ns signal.predicate.protocol)

(defprotocol IPredicate
  (check [this geojson-feature])
  (notification [this v]))

(defmulti make-predicate :type)
