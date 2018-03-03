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

(ns signal.specs.output
  (:require [clojure.spec.alpha :as spec]
            [xy.geojson :refer :all]
            [com.gfredericks.test.chuck.generators :as genc]
            [signal.specs.util :refer [email-regex,url-regex]]))

(spec/def :email/email (spec/with-gen #(re-matches email-regex %)
                         #(genc/string-from-regex email-regex)))
(spec/def :email/type #{"email"})
(spec/def :email/recipients (spec/coll-of :email/email))
(spec/def :output/email (spec/keys :req-un [:email/type :email/recipients]))

(spec/def :webhook/type #{"webhook"})
(spec/def :webhook/url (spec/with-gen #(re-matches url-regex %)
                         #(genc/string-from-regex url-regex)))
(spec/def :webhook/verb #{"get" "put" "post" "delete"})
(spec/def :output/webhook (spec/keys :req-un [:webhook/type :webhook/verb :webhook/url]))

(spec/def :wfs-t/url (spec/with-gen #(re-matches url-regex %)
                       #(genc/string-from-regex url-regex)))
(spec/def :wfs-t/type #{"wfs-t"})
(spec/def :wfs-t/geometry :xy.geojson/feature-spec)

(spec/def :output/wfs-t (spec/keys :req-un [:wfs-t/type :wfs-t/url :wfs-t/geometry]))

(spec/def ::output (spec/or :email :output/email
                            :webhook :output/webhook))
