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

(ns signal.specs.input
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [com.gfredericks.test.chuck.generators :as genc]
            [signal.specs.util :refer [url-regex,uuid-regex]]))

(spec/def ::id (spec/with-gen #(re-matches uuid-regex %)
                 #(gen/fmap (fn [u] (str u))
                            (gen/uuid))))

(spec/def ::name string?)
(spec/def ::description string?)
(spec/def ::interval (spec/with-gen pos-int?
                       #(spec/gen #{100})))

;;;;;;;;;;;;;;WFS;;;;;;;;;;;;;;;;
(spec/def :wfs-def/url (spec/with-gen #(re-matches url-regex %)
                         #(genc/string-from-regex url-regex)))
(spec/def :wfs/definition (spec/keys :req-un [:wfs-def/url ::interval]))
(spec/def :wfs/type #{"wfs"})
(spec/def ::input-wfs (spec/keys :req-un [::id ::name ::description :wfs/type :wfs/definition]))

;;;;;;;;;;;;;;HTTP;;;;;;;;;;;;;;;;
(spec/def :http/url (spec/with-gen :wfs-def/url
                      #(spec/gen #{"https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson"})))
(spec/def :http/interval pos-int?)
(spec/def :http/definition (spec/keys :req-un [:http/url ::interval]))
(spec/def :http/type #{"http"})
(spec/def ::input-http (spec/keys :req-un [::id ::name ::description :http/type :http/definition]))

;;;;;;;;;;;FINAL DEF;;;;;;;;;;;;;;
(spec/def ::input ::input-http)

(spec/def ::inputs (spec/coll-of ::input))
(spec/def ::input-ids (spec/or :all empty? :filtered (spec/coll-of ::id)))
