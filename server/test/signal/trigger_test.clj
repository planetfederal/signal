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

(ns signal.trigger-test
  (:require [clojure.test :refer :all]
            [signal.components.trigger.db :as trigger]
            [signal.test-utils :as utils]
            [signal.specs.trigger]
            [clojure.spec :as spec]
            [clojure.spec.gen :as gen]
            [camel-snake-kebab.core :refer :all]
            [camel-snake-kebab.extras :refer [transform-keys]]))

(use-fixtures :once utils/setup-fixtures)

(deftest all-trigger-test
  (is (true? (utils/spec-passed? `trigger/all))))

(defn generate-test-trigger []
  (gen/generate (spec/gen :signal.specs.trigger/trigger-spec)))

(deftest trigger-http-crud-test
  (let [test-trigger (generate-test-trigger)]

    (testing "Creating a trigger through REST api produces a valid HTML response"
      (let [res (utils/request-post "/api/triggers" test-trigger)
            new-trigger (:result res)]
        (is (contains? res :result)
            "Response should have result keyword")
        (is (spec/valid? :signal.specs.trigger/trigger-spec new-trigger)
            "The response should contain a trigger that conforms to the trigger spec")))

    (testing "Retrieving all triggers through REST api produces a valid HTML response"
      (let [res (-> (utils/request-get "/api/triggers"))
            trigger (->> res :result first)]
        (is (contains? res :result)
            "Response should have result keyword")
        (is (spec/valid? :signal.specs.trigger/trigger-spec trigger)
            "The response should contain a trigger that conforms to the trigger spec")))

    (testing "Retrieving trigger by its key through REST api produces a valid HTML response"
      (let [t (-> (utils/request-get "/api/triggers") :result first)
            res (-> (utils/request-get (str "/api/triggers/" (:id t))))
            trigger (:result res)]
        (is (contains? res :result)
            "Response should have result keyword")
        (is (spec/valid? :signal.specs.trigger/trigger-spec trigger)
            "The response should contain a trigger that conforms to the trigger spec")))

    (testing "Updating a trigger through REST api produces a valid HTML response"
      (let [trigger (-> (utils/request-get "/api/triggers") :result first)
            renamed-trigger (assoc trigger :name "foo")
            res (utils/request-put (str "/api/triggers/" (:id trigger)) renamed-trigger)
            updated-trigger (:result res)]
        (is (contains? res :result)
            "Response should have result keyword")
        (is (spec/valid? :signal.specs.trigger/trigger-spec updated-trigger)
            "The response should contain a trigger that conforms to the trigger spec")
        (is (= "foo" (:name updated-trigger))
            "The response should contain the updated trigger name")))

    (testing "Deleting triggers through REST api produces a valid HTML response"
      (let [trigger (-> (utils/request-get "/api/triggers") :result first)
            res (utils/request-delete (str "/api/triggers/" (:id trigger)))]
        (is (= "success" (:result res))
            "The response should contain a success message")))))
