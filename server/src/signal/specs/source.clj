(ns signal.specs.source
  (:require [clojure.spec :as s]))

(s/def :source/http string?)
(s/def :source/geojson string?)
(s/def :source/wfs string?)
(s/def ::source (s/or :http :source/http
                      :geojson :source/geojson
                      :wfs :source/wfs))
