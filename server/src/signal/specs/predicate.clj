(ns signal.specs.predicate
  (:require [clojure.spec :as s]
            [signal.specs.geojson]))

(s/def :predicate/geowithin/def :signal.specs.geojson/featurecollectionpolygon-spec)
(s/def :predicate/geowithin/type #{"$geowithin"})
(s/def :predicate/geowithin (s/keys :req-un [:predicate/geowithin/def :predicate/geowithin/type]))
(s/def ::predicates (s/or :geowithin :predicate/geowithin))
