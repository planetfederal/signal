(ns signal.filter.protocol)

(defprotocol IFilter
  (exec [this v]))

(defmulti make-filter :type)
