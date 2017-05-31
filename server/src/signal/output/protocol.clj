
(ns signal.output.protocol)

(defprotocol IOutput
  (send! [this v]))

(defmulti make-output :type)
