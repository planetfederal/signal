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

(ns signal.components.http.capabilities
  (:require [signal.components.http.intercept :as intercept]
            [signal.components.http.response :as response]
            [signal.predicate.geowithin :as predicate-geowithin]
            [signal.predicate.geodisjoint :as predicate-geodisjoint]
            [signal.predicate.identity :as predicate-identity]
            [signal.io.http :as io-http]
            [signal.io.mqtt :as io-mqtt]
            [signal.components.http.auth :refer [check-auth]]
            [signal.output.email :as output-email]
            [signal.output.webhook :as output-webhook]))

(def predicates
  #{{:type predicate-identity/identifier}
    {:type predicate-geowithin/identifier}
    {:type predicate-geodisjoint/identifier}})

(def inputs
  #{{:type io-http/identifier}
    {:type io-mqtt/identifier}})

(def outputs
  #{{:type output-email/identifier}
    {:type output-webhook/identifier}})

(defn http-get-all-capabilities
  "Returns http response of all capabilities"
  [_]
  (response/ok {:inputs inputs
                :outputs outputs
                :predicates predicates}))

(defn http-get-predicates [_] (response/ok predicates))
(defn http-get-inputs [_] (response/ok inputs))
(defn http-get-outputs [_] (response/ok outputs))

(defn routes []
  #{["/api/capabilities" :get
     (conj intercept/common-interceptors check-auth `http-get-all-capabilities)]
    ["/api/capabilities/predicates" :get
     (conj intercept/common-interceptors check-auth `http-get-predicates)]
    ["/api/capabilities/inputs" :get
     (conj intercept/common-interceptors check-auth `http-get-inputs)]
    ["/api/capabilities/outputs" :get
     (conj intercept/common-interceptors check-auth `http-get-outputs)]})
