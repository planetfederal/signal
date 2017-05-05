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
            [signal.specs.source]
            [signal.specs.filter]
            [signal.specs.reducer]
            [signal.specs.predicate]
            [signal.specs.sink]
            [signal.specs.geojson]))

(s/def :trigger/id pos-int?)
(s/def :trigger/name string?)
(s/def :trigger/description string?)
(s/def :trigger/repeated boolean?)
(s/def :trigger/persistent boolean?)

(s/def ::trigger-spec (s/keys :req-un
                              [:trigger/id :trigger/name :trigger/description
                               :trigger/repeated :trigger/persistent
                               :signal.specs.source/source
                               :signal.specs.filter/filters
                               :signal.specs.reducer/reducers
                               :signal.specs.predicate/predicates
                               :signal.specs.sink/sink]))