(ns signal.specs.sink
  (:require [clojure.spec :as s]))

(s/def :sink/kafka string?)
(s/def :sink/email (s/coll-of string?))
(s/def :sink/device (s/coll-of string?))
(s/def :sink/wfs string?)

(s/def ::sink (s/or :kafka :sink/kafka
                    :email :sink/email
                    :wfs :sink/wfs))
