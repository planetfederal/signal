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

(ns signal.output-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [signal.test-utils :as utils]
            [signal.components.input-manager :as input-api]
            [signal.components.processor :as processor-api]
            [signal.test-user :as user]
            [clojure.data.json :as json]))

(def input-id (java.util.UUID/randomUUID))
(def input {:type :http
            :interval 20
            :url "http://localhost:8085/api/test/webhook"
            :id input-id})

(def email-test-processor
  {:id (str (java.util.UUID/randomUUID))
   :name "email-test-processor"
   :description "An email test processor"
   :repeated false
   :persistent false
   :input-ids [input-id]
   :predicates [{:type :identity}]
   :output {:type :email
            :addresses ["wrichardet@boundlessgeo.com"]}})

(use-fixtures :once utils/setup-fixtures)

(def test-value {:id 1 :type "Feature"
                 :geometry {:type "Point" :coordinates [10.0 10.0]}
                 :properties {}})

(deftest email-processor-test
  (testing "Email Processor"
    (let [proc-comp (:processor user/system-val)]
      (processor-api/add-processor proc-comp email-test-processor)
      (is (some? (utils/request-post "/api/check" (json/write-str test-value)))))))

(def webhook-processor
  {:id (str (java.util.UUID/randomUUID))
   :name "test-webhook-processor"
   :description "test-webhook-processor description"
   :repeated false
   :input-ids [input-id]
   :output {:type :webhook
            :url "http://localhost:8085/api/test/webhook"
            :verb :post}})

(deftest webhook-output
  (testing "Webhook calls"
    (let [proc-comp (:processor user/system-val)
          input-comp (:input user/system-val)]
      (processor-api/add-processor proc-comp webhook-processor)
      (input-api/add-input input-comp input (partial processor-api/test-value proc-comp))
      (let [resp (utils/request-post "/api/check" (json/write-str test-value))]
        (is (= "success" (:result resp)))))))