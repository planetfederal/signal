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

(s/def :trigger/comparator-spec (s/with-gen
                                  (s/and string? #(contains? #{"$geowithin"} %))
                                  #(s/gen #{"$geowithin"})))

;;; spec for trigger
(s/def :trigger/name string?)
(s/def :trigger/description string?)
(s/def :trigger/stores (s/coll-of string?))
(s/def :trigger/emails (s/coll-of string?))
(s/def :trigger/devices (s/coll-of string?))
(s/def :trigger/recipients (s/keys :req-un [:trigger/emails :trigger/devices]))
(s/def :trigger/id pos-int?)
(s/def :trigger/lhs (s/coll-of string?))
(s/def :trigger/comparator :trigger/comparator-spec)
(s/def :trigger/rhs :signal.specs.geojson/featurecollectionpolygon-spec)
(s/def :trigger/rule (s/keys :req-un [:trigger/lhs :trigger/comparator :trigger/rhs :trigger/id]))
(s/def :trigger/rules (s/coll-of (s/or :no-rules empty? :some-rules :trigger/rule)))
(s/def :trigger/repeated boolean?)
(s/def ::trigger-spec (s/keys :req-un
                              [:trigger/name :trigger/description
                               :trigger/stores :trigger/recipients
                               :trigger/rules :trigger/repeated]))
