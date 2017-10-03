(ns signal.signal-test
  (:require [clojure.test :refer :all]
            [clojure.spec :as s]
            [signal.test-utils :as utils]
            [signal.components.input-manager :as input-api]
            [signal.components.processor :as processor-api]))

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

(use-fixtures :once utils/setup-fixtures)

(def test-value {:id 1 :type "Feature"
                 :geometry {:type "Point" :coordinates [10.0 10.0]}
                 :properties {}})

(deftest ^:integration processor
         (let [proc-comp (:processor user/system-val)
               input-comp (:input user/system-val)]
             (processor-api/add-processor proc-comp identity-processor)
             (input-api/add-input input-comp
                                  input
                                  (partial processor-api/test-value proc-comp))
             (processor-api/test-value proc-comp test-value)))
