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

(ns signal.specs.processor
  (:require [clojure.spec.alpha :as s]
            [signal.specs.input :as input]
            [signal.specs.filter :as filter]
            [signal.specs.reducer :as reducer]
            [signal.specs.predicate :as predicate]
            [signal.specs.mapper :as mapper]
            [signal.specs.output :as output]))

(s/def :processor/id pos-int?)
(s/def :processor/name string?)
(s/def :processor/description string?)
(s/def :processor/repeated boolean?)
(s/def :processor/persistent boolean?)

(s/def :processor/definition (s/keys
                               :req-un [:signal.specs.predicate/predicates]
                               :opt-un
                               [:signal.specs.mapper/mappers
                                :signal.specs.filter/filters
                                :signal.specs.reducer/reducers
                                :signal.specs.output/output]))

(s/def ::processor-spec (s/keys :req-un
                                [:processor/name
                                 :processor/description
                                 :processor/repeated
                                 :processor/definition]
                                :opt-un [:processor/id
                                         :signal.specs.input/input-ids
                                         :processor/persistent]))


