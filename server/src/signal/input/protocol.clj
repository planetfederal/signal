(ns signal.protocols.protocol)

(defprotocol IInput
  (start [this])
  (stop [this])
  (pause [this])
  (resume [this])
  (input-type [this]))