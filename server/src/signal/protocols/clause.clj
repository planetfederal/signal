(ns signal.protocols.clause)

(defprotocol IClause
  (field-path [this])
  (predicate [this])
  (check [this value])
  (notification [this v]))
