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

(ns signal.predicate.geowithin
  (:require [signal.predicate.protocol :as proto]
            [signal.utils.geo :as geo-util]
            [cljts.io :as jtsio]
            [xy.relations :as relations]
            [xy.geojson :as geojson]
            [cljts.relation :as spatial-relation]
            [clojure.data.json :as json]))

(def identifier :geowithin)

(defn notify [f]
  (json/write-str f))

(defrecord WithinClause [clause]
  proto/IPredicate
  (check [this geojson-feature]
    (relations/within? (:geometry geojson-feature)
                       (get-in this [:clause :geometry])))
  (notification [_ geojson-feature]
    (str (notify geojson-feature) " was within.")))

(defmethod proto/make-predicate identifier
  [predicate]
  (->WithinClause (geojson/parse (:def predicate))))
