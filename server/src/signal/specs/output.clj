(ns signal.specs.output
  (:require [clojure.spec :as s]))

(s/def ::date instance?)
(s/def ::to string?)
(s/def ::type :keyword)
(s/def ::message string?)
(s/def :output-spec (s/keys :req-un [::date]))