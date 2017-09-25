(ns signal.components.http.test
  (:require [signal.components.http.response :as response]
            [signal.components.http.intercept :as intercept]))

(defn- webhook-sample-get
  "Responds with  as a way to ensure http service is reachable"
  [_]
  (response/ok {:id 1
                :type "Point"
                :geometry [10.0 10.0 5.0]
                :properties {}}))

(defn- webhook-sample-accept
  [req]
  (response/ok {:response (:json-params req)}))

(defn routes
  []
  #{["/api/test/webhook" :get (conj intercept/common-interceptors `webhook-sample-get)]
    ["/api/tets/webhook" :post (conj intercept/common-interceptors `webhook-sample-accept)]})