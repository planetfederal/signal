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