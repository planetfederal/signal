(ns signal.rest-test
  (:require [signal.test-utils :as utils]
            [clojure.test :refer :all]
            [clojure.spec.gen.alpha :as spec-gen]
            [clojure.spec.alpha :as spec]
            [signal.specs.input :refer :all]
            [clojure.data.json :as json]))

(use-fixtures :once utils/setup-fixtures)

(deftest input-rest-test
  (testing "Input REST"
    (let [sample-input (-> (spec/gen :signal.specs.input/input-http) spec-gen/sample first)
          res-post (utils/request-post "/api/inputs" (json/write-str sample-input))
          id (get-in res-post [:result :id])
          new-input (assoc sample-input :id id)
          query-input (:result (utils/request-get (str "/api/inputs/" id)))
          query-after (utils/request-get (str "/api/inputs/" id))
          res-delete (utils/request-delete (str "/api/inputs/" id))]
      (is (some? (:result res-post)))
      (is (= (:id new-input) (:id query-input)))
      (is (= (:type new-input) (:type query-input)))
      (is (= (:definition new-input) (:definition query-input)))
      (is (= (:result res-delete) "success"))
      (is (= (:result query-after) (:result res-post))))))

(deftest processor-rest-test
  (testing "processor REST"
    (let [sample-processor (-> (spec/gen :signal.specs.processor/processor-spec)
                               spec-gen/sample first)
          res-post (utils/request-post "/api/processors" (json/write-str sample-processor))
          id (get-in res-post [:result :id])
          new-processor (assoc sample-processor :id id)
          query-processor (:result (utils/request-get (str "/api/processors/" id)))
          query-after (utils/request-get (str "/api/processors/" id))
          res-delete (utils/request-delete (str "/api/processors/" id))]
      (is (some? (:result res-post)))
      (is (= (:id new-processor) (:id query-processor)))
      (is (= (:type new-processor) (:type query-processor)))
      (is (= (:definition new-processor) (:definition query-processor)))
      (is (= (:result res-delete) "success"))
      (is (= (:result query-after) (:result res-post) )))))
