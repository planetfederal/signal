(ns signal.input.stream-proto)

(defprotocol IStreamingInput
  (start [this])
  (stop [this])
  (recv [this]))

(defmulti make-streaming-input :type)