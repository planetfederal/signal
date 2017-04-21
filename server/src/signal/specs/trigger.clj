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

(ns signal.specs.trigger
  (:require [clojure.spec :as s]
            [signal.specs.geojson]))

(s/def :source/http string?)
(s/def :source/geojson string?)
(s/def :source/wfs string?)
(s/def ::source (s/or :http :source/http 
                     :geojson :source/geojson 
                     :wfs :source/wfs))

(s/def :filter/identity/type #{"identity"})
(s/def :filter/identity (s/keys :req [:filter/identity/type]))
(s/def ::filters (s/or :identity :filter/identity))

(s/def :reducer/identity/type #{"identity"})
(s/def :reducer/identity (s/keys :req [:reducer/identity/type]))
(s/def ::reducers (s/or :identity :reducer/identity))

(s/def :predicate/geowithin/def :signal.specs.geojson/featurecollectionpolygon-spec)
(s/def :predicate/geowithin/type #{"$geowithin"})
(s/def :predicate/geowithin (s/keys :predicate/geowithin/def :predicate/geowithin/type))
(s/def :predicate/available (s/or :geowithin :predicate/geowithin))
(s/def ::predicate :predicate/available)

(s/def :sink/kafka string?)
(s/def :sink/email (s/coll-of string?))
(s/def :sink/device (s/coll-of string?))
(s/def :sink/wfs string?)
(s/def ::sink (s/or :kafka :sink/kafka 
                    :email :sink/email 
                    :wfs :sink/wfs))

(s/def :rule/id pos-int?)
(s/def :rule/name string?)
(s/def :rule/description string?)
(s/def :rule/repeated boolean?)
(s/def :rule/sources (s/or :http :source/http 
                           :geojson :source/geojson 
                           :wfs :source/wfs))

(s/def :rule/filters (s/coll-of :reducer/identity))
(s/def :rule/reducers (s/coll-of :reducer/identity))
(s/def :rule/predicates (s/coll-of :predicate))
(s/def :rule/sink (s/or :kafka :sink/kafka 
                        :email :sink/email 
                        :device :sink/device 
                        :wfs :sink/wfs))

(s/def ::rule-spec (s/keys :req-un
                              [:rule/name :rule/description
                               :rule/repeated
                               :rule/sources :rule/filters :rule/reducers :rule/predicates :rule/sink]))
