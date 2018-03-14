(ns signal.input.stream-proto)

(defprotocol IStreamingInput
  (start [this func])
  (stop [this]))

(defmulti make-streaming-input :type)
