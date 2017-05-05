(ns signal.specs.reducer
  (:require [clojure.spec :as s]))

(s/def :identity/type #{"identity"})
(s/def :identity/reducer (s/keys :req-un [:identity/type]))

(s/def ::reducers (s/coll-of (s/or :identity :identity/reducer)))
