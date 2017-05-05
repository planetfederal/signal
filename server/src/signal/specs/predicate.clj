(ns signal.specs.predicate
  (:require [clojure.spec :as s]
            [signal.specs.geojson]))

(s/def :geowithin/def :signal.specs.geojson/featurecollectionpolygon-spec)
(s/def :geowithin/type #{"$geowithin"})
(s/def :predicate/geowithin (s/keys :req-un [:geowithin/def :geowithin/type]))
(s/def ::predicates (s/coll-of (s/or :geowithin :predicate/geowithin)))
