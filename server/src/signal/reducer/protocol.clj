(ns signal.reducer.protocol)

(defprotocol IReducer
  (exec [this v]))

(defmulti make-reducer :type)