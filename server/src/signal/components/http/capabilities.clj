(ns signal.components.http.capabilities
  (:require [signal.components.http.intercept :as intercept]
            [signal.components.http.response :as response]
            [signal.mapper.identity :as mapper-identity]
            [signal.filter.identity :as filter-identity]
            [signal.reducer.identity :as reducer-identity]
            [signal.predicate.geowithin :as predicate-geowithin]
            [signal.predicate.identity :as predicate-identity]
            [signal.input.http :as input-http]
            [signal.output.email :as output-email]))

(def mappers
  #{{:type mapper-identity/identitifer}})

(def filters
  #{{:type filter-identity/identifier}})

(def reducers
  #{{:type reducer-identity/identifier}})

(def predicates
  #{{:type predicate-identity/identifier}
    {:type predicate-geowithin/identifier}})

(def inputs
  #{{:type input-http/identifier}})

(def outputs
  #{{:type output-email/identifier}})

(defn http-get-all-capabilities
  "Returns http response of all capabilities"
  [_]
  (response/ok {:inputs inputs
                :outputs outputs
                :predicates predicates
                :reducers reducers
                :mappers mappers
                :filters filters}))

(defn http-get-mappers [_] (response/ok mappers))
(defn http-get-filters [_] (response/ok filters))
(defn http-get-reducers [_] (response/ok reducers))
(defn http-get-predicates [_] (response/ok predicates))
(defn http-get-inputs [_] (response/ok inputs))
(defn http-get-outputs [_] (response/ok outputs))

(defn routes []
  #{["/api/capabilities" :get
     (conj intercept/common-interceptors `http-get-all-capabilities)]
    ["/api/capabilities/mappers" :get
     (conj intercept/common-interceptors `http-get-mappers)]
    ["/api/capabilities/filters" :get
     (conj intercept/common-interceptors `http-get-filters)]
    ["/api/capabilities/reducers" :get
     (conj intercept/common-interceptors `http-get-reducers)]
    ["/api/capabilities/predicates" :get
     (conj intercept/common-interceptors `http-get-predicates)]
    ["/api/capabilities/inputs" :get
     (conj intercept/common-interceptors `http-get-inputs)]
    ["/api/capabilities/outputs" :get
     (conj intercept/common-interceptors `http-get-outputs)]})

