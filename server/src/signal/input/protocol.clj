(ns signal.protocols.protocol)

(defprotocol IInput
  (recv [this v]))

(defmulti make-input :type)