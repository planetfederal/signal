(ns signal.input.stream-proto)

(defprotocol IStreamingInput
  (start [this])
  (stop [this]))

(defmulti make-streaming-input :type)