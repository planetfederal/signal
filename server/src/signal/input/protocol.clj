(ns signal.input.protocol)

(defprotocol IInput
  (recv [this v]))

(defmulti make-input :type)