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

(ns signal.utils.geo
  (:require [cljts.relation :as spatial-relation]
            [clojure.data.json :as json]
            [cljts.io :as jtsio]))

(defn geojsonmap->jtsgeom
  "Takes a clojure map of the geojson and returns it as
  a JTS default feature collection"
  [c]
  (jtsio/read-feature-collection
    (json/write-str c)))

(defn check-simple-feature
  "Returns true if point is in feature"
  [point feature func]
  (if (or (nil? point) (nil? feature))
    false
    (func point feature)))

(defn check-feature-collection
  "Returns true if point is within any geometry in feature-collection"
  [point feature-collection func]
  (if-let [features (.features feature-collection)]
    (if (.hasNext features)
      (loop [feature (-> features .next .getDefaultGeometry)]
        (if (check-simple-feature point feature func)
          true
          (if (.hasNext features)
            (recur (-> features .next .getDefaultGeometry))
            false))); fails within, false
      false) ; has no .next, false
    false)); has no features, false

(def clause-case-map {org.geotools.feature.DefaultFeatureCollection check-feature-collection,
                      org.geotools.feature.simple.SimpleFeatureImpl check-simple-feature})