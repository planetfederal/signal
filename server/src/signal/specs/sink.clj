(ns signal.specs.sink
  (:require [clojure.spec :as s]
            [com.gfredericks.test.chuck.generators :as genc]))

(s/def :kafka/type #{"kafka"})
(s/def :kafka/topic string?)
(s/def :sink/kafka (s/keys :req-un [:kafka/type :kafka/topic]))

(def email-regex #"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}")
(s/def :email/email (s/with-gen #(re-matches email-regex %)
                               #(genc/string-from-regex email-regex)))
(s/def :email/type #{"email"})
(s/def :email/addresses (s/coll-of :email/email))
(s/def :sink/email (s/keys :req-un [:email/type :email/addresses]))

(def url-regex #"((http[s]?|ftp):\/)?\/?([^:\/\s]+)((\/\w+)*\/)([\w\-\.]+[^#?\s]+)(.*)?(#[\w\-]+)?")
(s/def :wfs/url (s/with-gen #(re-matches url-regex %)
                            #(genc/string-from-regex url-regex)))
(s/def :wfs/type #{"wfs"})
(s/def :sink/wfs (s/keys :req-un [:wfs/type :wfs/url]))

(s/def :sink/device (s/coll-of string?))

(s/def ::sink (s/or :kafka :sink/kafka
                    :email :sink/email
                    :device :sink/device
                    :wfs :sink/wfs))
