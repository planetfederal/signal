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

(ns signal.test-utils
  (:require [clojure.spec.test.alpha :as stest]
            [clojure.walk :refer [keywordize-keys]]
            [io.pedestal.test :refer [response-for]]
            [clojure.data.json :as json]
            [signal.test-user :as user]))

(defn spec-passed? [s] (-> (stest/check s
                                        {:clojure.spec.test.check/opts
                                         {:num-tests 50}})
                           first
                           :clojure.spec.test.check/ret
                           :result))

(defn service-def []
  (:io.pedestal.http/service-fn (:http-server (:server user/system-val))))

(def auth-header (atom {}))

(defn get-response-for
  [method url body & [headers]]
  (response-for (service-def)
                method url
                :body (json/write-str body)
                :headers (merge {"Content-Type" "application/json"}
                                @auth-header
                                headers)))

(defn request-get [url & [headers]]
  (let [res (get-response-for :get url nil headers)]
    (keywordize-keys (json/read-str (:body res)))))

(defn request-post [url body & [headers]]
  (let [res (get-response-for :post url body headers)]
    (keywordize-keys (json/read-str (:body res)))))

(defn request-put [url body & [headers]]
  (let [res (get-response-for :put url body headers)]
    (keywordize-keys (json/read-str (:body res)))))

(defn request-delete [url & [headers]]
  (let [res (get-response-for :delete url nil headers)]
    (keywordize-keys (json/read-str (:body res)))))

(defn- authenticate [user pass]
  (let [res (request-post "/api/authenticate" {:email user :password pass})
        token (get-in res [:result :token])]
    {"Authorization" (str "Token " token)}))

(defn setup-fixtures [f]
  (user/go)
  (f)
  (user/stop))
