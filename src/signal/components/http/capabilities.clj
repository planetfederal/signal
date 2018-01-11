(ns signal.components.http.capabilities
  (:require [signal.components.http.intercept :as intercept]
            [signal.components.http.response :as response]
            [signal.predicate.geowithin :as predicate-geowithin]
            [signal.predicate.geodisjoint :as predicate-geodisjoint]
            [signal.predicate.identity :as predicate-identity]
            [signal.input.http :as input-http]
            [signal.components.http.auth :refer [check-auth]]
            [signal.output.email :as output-email]
            [signal.output.webhook :as output-webhook]))

(def predicates
  #{{:type predicate-identity/identifier}
    {:type predicate-geowithin/identifier}
    {:type predicate-geodisjoint/identifier}})

(def inputs
  #{{:type input-http/identifier}})

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

