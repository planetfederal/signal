;; Copyright 2016-2017 Boundless, http://boundlessgeo.com
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns signal.specs.predicate
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [xy.geojson :refer :all]
            [signal.specs.regex :refer [uuid-regex]]
            [com.gfredericks.test.chuck.generators :as genc]))

(spec/def :predicate/id (spec/with-gen #(re-matches uuid-regex %)
                                       #(gen/fmap (fn [u] (str u))
                                                  (gen/uuid))))

(spec/def :geowithin/definition :xy.geojson/featurecollectionpolygon-spec)
(spec/def :geowithin/type #{"geowithin"})
(spec/def :predicate/geowithin (spec/keys :req-un [:predicate/id :geowithin/definition :geowithin/type]))

(spec/def :identity/type #{"identity"})
(spec/def :predicate/identity (spec/keys :req-un [:predicate/id :identity/type]))

(spec/def ::predicates (spec/coll-of (spec/or :geowithin :predicate/geowithin
                                              :identity :predicate/identity)))
