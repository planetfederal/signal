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

(ns signal.specs.geojson
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]))

(defn circle-gen [x y]
  (let [vertices (+ (rand-int 8) 4)
        radius (rand 3) ;2 dec degrees radius length
        rads (/ (* 2.0 Math/PI) vertices)
        pts (map (fn [r]
                   [(+ x (* radius (Math/cos (* r rads))))
                    (+ y (* radius (Math/sin (* rads r))))])
                 (range vertices))]
    (conj pts (last pts))))

(defn line-gen [x y cnt]
  (let [vertices  (+ cnt 2)]
    (map (fn  []
           [(+ x (rand))
            (+ y (rand))])
         (range vertices))))

;;; geojson
(s/def :gj/x (s/double-in :min -175.0 :max 175.0 :NaN? false :infinite? false))
(s/def :gj/y (s/double-in :min -85.0 :max 85.0 :NaN? false :infinite? false))
(s/def :gj/coordinates (s/with-gen
                         coll?
                         #(gen/fmap (fn [[lon lat]] (list lon lat))
                                    (gen/tuple (s/gen :gj/x) (s/gen :gj/y)))))
(s/def :gjpt/type (s/with-gen string? #(s/gen #{"Point"})))
(s/def :gjls/coordinates
  (s/with-gen
    coll?
    #(gen/fmap (fn
                 [[lon lat cnt]]
                 (line-gen lon lat cnt))
               (gen/tuple (s/gen :gj/x) (s/gen :gj/y) (s/gen pos-int?)))))
(s/def :gjls/type (s/with-gen string? #(s/gen #{"LineString"})))
(s/def :gjpl/coordinates (s/with-gen
                           coll?
                           #(gen/fmap (fn [[lon lat]] (list (circle-gen lon lat)))
                                      (gen/tuple (s/gen :gj/x) (s/gen :gj/y)))))
(s/def :gjpl/type (s/with-gen string? #(s/gen #{"Polygon"})))
(s/def :gjmpt/coordinates (s/coll-of :gj/coordinates))
(s/def :gjmpt/type (s/with-gen string? #(s/gen #{"MultiPoint"})))
(s/def :gjmls/coordinates (s/coll-of :gjls/coordinates))
(s/def :gjmls/type (s/with-gen string? #(s/gen #{"MultiLineString"})))
(s/def :gjmpl/coordinates (s/coll-of :gjpl/coordinates))
(s/def :gjmpl/type (s/with-gen string? #(s/gen #{"MultiPolygon"})))

(def geom-types #{"Point" "Polygon" "LineString"
                  "MultiPolygon" "MultiLinestring" "MultiPoint"})
(s/def :gj/point (s/keys :req-un [:gjpt/type :gj/coordinates]))
(s/def :gj/linestring (s/keys :req-un [:gjls/type :gjls/coordinates]))
(s/def :gj/polygon (s/keys :req-un [:gjpl/type :gjpl/coordinates]))
(s/def :gj/multipoint (s/keys :req [:gjmpt/type :gjmpt/coordinates]))
(s/def :gj/multilinestring (s/keys :req [:gjmls/type :gjmls/coordinates]))
(s/def :gj/multipolygon (s/keys :req [:gjmpl/type :gjmpl/coordinates]))

(s/def :gj/type (s/with-gen
                  (s/and string? #(contains? geom-types %))
                  #(s/gen geom-types)))
(s/def :gj/geometrytypes (s/or :point :gj/point
                               :linestring :gj/linestring
                               :polygon :gj/polygon
                               :multipoint :gj/multipoint
                               :multilinestring :gj/multilinestring
                               :multipolygon :gj/multipolygon))
(s/def :gjpt/geometry :gj/point)
(s/def :gjpl/geometry :gj/polygon)
(s/def :gjls/geometry :gj/linestring)
(s/def :gj/geometry :gj/geometrytypes)
(s/def :gfeature/id (s/and string? #(> (count %) 0)))
(s/def :gfeature/properties (s/with-gen
                              (s/or :nil nil? :map map?)
                              #(s/gen #{{}})))
(s/def :gfeature/type #{"Feature"})
; Single geojson point feature
(s/def ::pointfeature-spec (s/keys :req-un
                                   [:gfeature/id :gfeature/type
                                    :gfeature/properties :gjpt/geometry]))
; Single geojson polygon feature
(s/def ::polygonfeature-spec (s/keys :req-un
                                     [:gfeature/id :gfeature/type
                                      :gfeature/properties :gjpl/geometry]))
; Single geojson linestring feature
(s/def ::linestringfeature-spec (s/keys :req-un
                                        [:gfeature/id :gfeature/type
                                         :gfeature/properties :gjls/geometry]))

; Single geojson feature
(s/def ::feature-spec (s/keys :req-un
                              [:gfeature/id :gfeature/type
                               :gj/geometry :gfeature/properties]))
(s/def :gj/features (s/coll-of ::feature-spec))
(s/def :gjpoly/features (s/coll-of ::polygonfeature-spec :min-count 1))
(s/def :fcgj/type (s/with-gen
                    (s/and string? #(contains? #{"FeatureCollection"} %))
                    #(s/gen #{"FeatureCollection"})))
(s/def ::featurecollection-spec (s/keys :req-un [:fcgj/type :gj/features]))
(s/def ::featurecollectionpolygon-spec (s/keys :req-un
                                               [:fcgj/type :gjpoly/features]))
