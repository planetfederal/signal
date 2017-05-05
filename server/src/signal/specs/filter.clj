(ns signal.specs.filter
  (:require [clojure.spec :as s]))

(s/def :identity/type #{"identity"})
(s/def :identity/filter (s/keys :req-un [:identity/type]))

(s/def ::filters (s/coll-of (s/or :identity :identity/filter)))