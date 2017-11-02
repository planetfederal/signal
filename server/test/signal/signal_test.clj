(ns signal.signal-test
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
            :interval 0
            :url "http://localhost:8085/api/test/webhook"
            :id input-id})

(def identity-processor
  {:id (str (java.util.UUID/randomUUID))
   :name "test-processor"
   :description "A test processor"
   :repeated false
   :persistent false
   :input-ids [input-id]
   :mappers [{:type :identity}]
   :filters [{:type :identity}]
   :reducers [{:type :identity}]
   :predicates [{:type :identity}]
   :output {:type :webhook
            :url "http://localhost:8085/api/test/webhook"
            :verb :post}})

(def geowithin-processor
  {:id (str (java.util.UUID/randomUUID))
   :name "geowithin-test-processor"
   :repeated false
   :persistent false
   :input-ids [input-id]
   :mappers [{:type :identity}]
   :filters [{:type :identity}]
   :reducers [{:type :identity}]
   :predicates [{:type :geowithin
                 :def {:id 2 :type "Feature"
                       :geometry {:type "Polygon"
                                  :coordinates [[[0.0 0.0]
                                                 [0.0 20.0]
                                                 [20.0 20.0]
                                                 [20.0 0.0]
                                                 [0.0 0.0]]]}
                       :properties {}}}]
   :output {:type :webhook
            :url "http://localhost:8085/api/test/webhook"
            :verb :post}})

(use-fixtures :once utils/setup-fixtures)

(def test-value {:id 1 :type "Feature"
                 :geometry {:type "Point" :coordinates [10.0 10.0]}
                 :properties {}})

(deftest ^:integration identity-processor-test
  (testing "Identity Processor"
    (let [proc-comp (:processor user/system-val)
          input-comp (:input user/system-val)]
      (processor-api/add-processor proc-comp identity-processor)
      (input-api/add-input input-comp
                          input
                          (partial processor-api/test-value proc-comp))
      (is (some? (utils/request-post "/api/check" (json/write-str test-value)))))))

(deftest ^:integration geowithin-processor-test
  (testing "Identity Processor"
    (let [proc-comp (:processor user/system-val)
          input-comp (:input user/system-val)]
      (processor-api/add-processor proc-comp geowithin-processor)
      (input-api/add-input input-comp
                           input
                           (partial processor-api/test-value proc-comp))
      (is (some? (utils/request-post "/api/check" (json/write-str test-value)))))))
