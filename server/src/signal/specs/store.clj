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

(ns signal.specs.store
  (:require [clojure.spec :as s]
            [clojure.test.check.generators :as gen]))

(defn uuid-string-gen []
  (->>
   (gen/uuid)
   (gen/fmap #(.toString %))))

;; define specs about store
(def uuid-regex #"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
(s/def :store/id (s/with-gen
                   (s/and string? #(re-matches uuid-regex %))
                   #(uuid-string-gen)))
(s/def :store/store_type #{"geojson" "gpkg" "wfs"})
(s/def :store/version string?)
(s/def :store/uri string?)
(s/def :store/name string?)
(s/def :store/default_layers (s/coll-of string?))
(s/def ::store-spec (s/keys :req-un [:store/name :store/store_type
                                     :store/version :store/uri
                                     :store/default_layers]))
