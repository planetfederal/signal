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

(ns signal.ping-test
  (:require [clojure.test :refer :all]
            [signal.test-utils :as utils]))

(use-fixtures :once utils/setup-fixtures)

(deftest ping-test []
  (let [res (utils/request-get "/api/ping")]
    (is (= (:result res) "pong"))))
