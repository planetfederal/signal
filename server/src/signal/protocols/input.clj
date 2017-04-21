(ns signal.protocols.input)

(defprotocol IInput
  (start [this])
  (stop [this])
  (pause [this])
  (resume [this])
  (input-type [this]))