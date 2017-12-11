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

(ns signal.predicate-test
  (:require [clojure.test :refer :all]
            [signal.test-utils :as utils]
            [signal.components.input-manager :as input-api]
            [signal.components.processor :as processor-api]
            [signal.test-user :as user]
            [clojure.data.json :as json]))

(def input-id (java.util.UUID/randomUUID))

(def input {:type       "http"
            :definition {:interval 0
                         :url      "http://localhost:8085/api/test/webhook"}
            :id         input-id})

(def test-value {:id         1
                 :type       "Feature"
                 :geometry   {:type "Point" :coordinates [10.0 10.0]}
                 :properties {}})

(use-fixtures :once utils/setup-fixtures)

(def identity-processor
  {:id          (str (java.util.UUID/randomUUID))
   :name        "test-processor"
   :description "A test processor"
   :repeated    false
   :persistent  false
   :input-ids   [input-id]
   :definition  {:predicates [{:type "identity"}]
                 :output     {:type "webhook"
                              :url  "http://localhost:8085/api/test/webhook"
                              :verb "post"}}})

(deftest identity-processor-test
  (testing "Identity Processor"
    (let [proc-comp (:processor user/system-val)
          input-comp (:input user/system-val)]
      (processor-api/add-processor proc-comp identity-processor)
      (input-api/add-polling-input input-comp input)
      (is (some? (utils/request-post "/api/check" (json/write-str test-value)))))))

(def geowithin-processor
  {:id         (str (java.util.UUID/randomUUID))
   :name       "geowithin-test-processor"
   :repeated   false
   :persistent false
   :input-ids  [input-id]
   :definition {:predicates [{:type       "geowithin"
                              :definition {:id 2
                                           :type       "Feature"
                                           :geometry   {:type        "Polygon"
                                                        :coordinates [[[0.0 0.0]
                                                                       [0.0 20.0]
                                                                       [20.0 20.0]
                                                                       [20.0 0.0]
                                                                       [0.0 0.0]]]}
                                           :properties {}}}]
                :output     {:type "webhook"
                             :url  "http://localhost:8085/api/test/webhook"
                             :verb :post}}})

(deftest geowithin-processor-test
  (testing "Geowithin Processor"
    (let [proc-comp (:processor user/system-val)
          input-comp (:input user/system-val)]
      (processor-api/add-processor proc-comp geowithin-processor)
      (input-api/add-polling-input input-comp
                                   input)
      (is (some? (utils/request-post "/api/check" (json/write-str test-value)))))))

(def geodisjoint-processor
  {:id         (str (java.util.UUID/randomUUID))
   :name       "geodisjoint-test-processor"
   :repeated   false
   :persistent false
   :input-ids  [input-id]
   :definition {:predicates [{:type "geodisjoint"
                              :definition
                                    {:id         2 :type "Feature"
                                     :geometry   {:type        "Polygon"
                                                  :coordinates [[[0.0 0.0]
                                                                 [0.0 20.0]
                                                                 [20.0 20.0]
                                                                 [20.0 0.0]
                                                                 [0.0 0.0]]]}
                                     :properties {}}}]
                :output     {:type "webhook"
                             :url  "http://localhost:8085/api/test/webhook"
                             :verb "post"}}})

(deftest geodisjoint-processor-test
  (testing "Geodisjoint Processor"
    (let [proc-comp (:processor user/system-val)
          input-comp (:input user/system-val)]
      (processor-api/add-processor proc-comp geodisjoint-processor)
      (input-api/add-polling-input input-comp
                                   input)
      (is (some? (utils/request-post "/api/check" (json/write-str test-value)))))))
