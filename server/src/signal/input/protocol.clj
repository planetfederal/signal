(ns signal.input.protocol)

(defprotocol IInput
  (interval [this])
  (recv [this v]))

(defmulti make-input :type)