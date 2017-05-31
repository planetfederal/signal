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
