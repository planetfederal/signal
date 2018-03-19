;; Copyright 2016-2018 Boundless, http://boundlessgeo.com
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

(ns signal.components.http.processor
  (:require
   [signal.components.http.intercept :as intercept]
   [signal.components.http.response :as response]
   [clojure.data.json :as json]
   [xy.geojson :as geojson]
   [signal.components.http.auth :refer [check-auth]]
   [clojure.tools.logging :as log]
   [signal.components.processor :as processor-api]))

(defn http-get-all-processors
  "Returns http response of all processors"
  [processor-comp _]
  (log/debug "Getting all processors")
  (response/ok (processor-api/all processor-comp)))

(defn http-get-processor
  "Gets a processor by id"
  [processor-comp request]
  (log/debug "Getting processor by id")
  (let [id (get-in request [:path-params :id])]
    (if-let [processor (processor-api/find-by-id processor-comp id)]
      (response/ok processor)
      (let [err-msg (str "No processor found for id" id)]
        (log/warn err-msg)
        (response/ok err-msg)))))

(defn http-put-processor
  "Updates a processor using the json body"
  [processor-comp request]
  (log/debug "Updating processor")
  (let [t (:json-params request)]
    (log/debug "Validating processor")
    (let [processor (processor-api/modify processor-comp
                                          (get-in request [:path-params :id]) t)]
      (response/ok processor))))

(defn http-post-processor
  "Creates a new processor using the json body"
  [processor-comp request]
  (log/debug "Adding new processor")
  (let [t (:json-params request)
        processor (processor-api/add-processor processor-comp t)]
    (response/ok processor)))

(defn http-delete-processor
  "Deletes a processor"
  [processor-comp request]
  (log/debug "Deleting processor")
  (let [id (get-in request [:path-params :id])]
    (processor-api/delete processor-comp id)
    (response/ok "success")))

(defn http-test-processor
  "HTTP endpoint used to test processors.  Takes a geojson feature
  in the json body as the feature to test"
  [processor-comp request]
  (if-let [params (:json-params request)]
    (do
      (processor-api/test-value processor-comp (geojson/parse params))
      (response/ok "success"))
    (response/bad-request "Request was empty")))

(defn routes [processor-comp]
  #{["/api/processors" :get
     (conj intercept/common-interceptors check-auth
           (partial http-get-all-processors processor-comp))
     :route-name :get-processors]
    ["/api/processors/:id" :get
     (conj intercept/common-interceptors check-auth
           (partial http-get-processor processor-comp))
     :route-name :get-processor]
    ["/api/processors/:id" :put
     (conj intercept/common-interceptors check-auth
           (partial http-put-processor processor-comp))
     :route-name :put-processor]
    ["/api/processors" :post
     (conj intercept/common-interceptors check-auth
           (partial http-post-processor processor-comp))
     :route-name :post-processor]
    ["/api/processors/:id" :delete
     (conj intercept/common-interceptors check-auth
           (partial http-delete-processor processor-comp))
     :route-name :delete-processor]
    ["/api/check" :post
     (conj intercept/common-interceptors check-auth
           (partial http-test-processor processor-comp))
     :route-name :http-test-processor]})
