(ns signal.input.poll-proto
  (:import (com.sun.imageio.spi InputStreamImageInputStreamSpi)))

(defprotocol IPollingInput
  (interval [this])
  (poll [this v]))

(defmulti make-polling-input :type)
