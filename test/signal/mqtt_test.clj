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

(ns signal.mqtt-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [signal.test-utils :as utils]
            [signal.components.input-manager :as input-api]
            [signal.components.processor :as processor-api]
            [signal.test-user :as user]
            [clojure.data.json :as json]
            [clojurewerkz.machine-head.client :as mh]))

(def input-id (.toString (java.util.UUID/randomUUID)))
(def input {:type :mqtt
            :url "tcp://localhost"
            :port 1883
            :topic "/foo"
            :id "TestIdMQTTConsumer"})

(defn output-fn
  [value]
  (is (some? value)))

(def mqtt-test-processor
  {:id (str (java.util.UUID/randomUUID))
   :name "mqtt-test-processor"
   :description "An mqtt test processor"
   :repeated false
   :persistent false
   :input-ids [input-id]
   :predicates [{:type :identity}]
   :output {:type :test
            :output-fn output-fn}})

(use-fixtures :once utils/setup-fixtures)

(def test-value {:id 1 :type "Feature"
                 :geometry {:type "Point" :coordinates [10.0 10.0]}
                 :properties {}})

(defn send-test-mqtt []
  (let [client (mh/connect (str (:url input) ":" (:port input))
                           "TestMQTTProducer")
        payload (-> test-value json/write-str .getBytes)]
    (mh/publish client (:topic input) payload)))

(deftest ^:integration mqtt-test
  (testing "MQTT Input"
    (let [proc-comp (:processor user/system-val)
          input-comp (:input user/system-val)]
      (processor-api/add-processor proc-comp mqtt-test-processor)
      (input-api/add-streaming-input input-comp input)
      (send-test-mqtt)
      (Thread/sleep 1000))))

