(ns signal.specs.input
  (:require [clojure.spec :as s]
            [signal.specs.output]
            [com.gfredericks.test.chuck.generators :as genc]))

(def url-regex #"((http[s]?|ftp):\/)?\/?([^:\/\s]+)((\/\w+)*\/)([\w\-\.]+[^#?\s]+)(.*)?(#[\w\-]+)?")

(s/def :wfs/url (s/with-gen #(re-matches url-regex %)
                            #(genc/string-from-regex url-regex)))
(s/def :wfs/type #{"wfs"})
(s/def :input/wfs (s/keys :req-un [:wfs/type :wfs/url]))

(s/def :http/url :wfs/url)
(s/def :http/type #{"http"})
(s/def :input/http (s/keys :req-un [:http/url :http/type]))

(s/def :geojson/url :wfs/url)
(s/def :geojson/type #{"geojson"})
(s/def :input/geojson (s/keys :req-un [:geojson/url :geojson/type]))

(s/def ::input (s/or :http :input/http
                      :geojson :input/geojson
                      :wfs :input/wfs))
