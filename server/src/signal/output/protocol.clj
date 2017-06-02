(ns signal.output.protocol)

(defprotocol IOutput
  (recipients [this])
  (send! [this v]))

(defmulti make-output :type)
