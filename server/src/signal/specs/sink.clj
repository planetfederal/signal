(ns signal.specs.sink
  (:require [clojure.spec :as s]
            [com.gfredericks.test.chuck.generators :as genc]
            [clojure.test.check.generators :as gen]))

(s/def :kafka/type #{"kafka"})
(s/def :kafka/topic string?)
(s/def :sink/kafka (s/keys :req-un [:kafka/type :kafka/topic]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def :email/type (s/with-gen #{:email}
                               #(genc/string-from-regex email-regex)))

(s/def :email/addresses (s/coll-of :email/type))
(s/def :sink/email (s/keys :req-un [:email/type :email/addresses]))
(s/def :sink/device (s/coll-of string?))
(s/def :sink/wfs string?)

(s/def ::sink (s/or :kafka :sink/kafka
                    :email :sink/email
                    :wfs :sink/wfs))
