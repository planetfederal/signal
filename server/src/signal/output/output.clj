(ns signal.protocols.output)

(defprotocol IOutput
  (send [this]))