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
  (:require [clojure.spec :as spec]
            [com.gfredericks.test.chuck.generators :as genc]))

(def email-regex #"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}")
(spec/def :email/email (spec/with-gen #(re-matches email-regex %)
                                      #(genc/string-from-regex email-regex)))
(spec/def :email/type #{"email"})
(spec/def :email/addresses (spec/coll-of :email/email))
(spec/def :output/email (spec/keys :req-un [:email/type :email/addresses]))

(spec/def ::output (spec/or :email :output/email))
