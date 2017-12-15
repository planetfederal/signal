(ns signal.components.http.ping
  (:require [signal.components.http.response :as response]
            [signal.components.http.intercept :as intercept]
            [clojure.tools.logging :as log]))

(defn- pong
  "Responds with pong as a way to ensure http service is reachable"
  [_]
  (response/ok "pong"))

(defn- pong-post
  "This echos the post body"
  [request]
  (do
    (log/debug (:json-params request))
    (response/ok (:json-params request))))

(defn routes
  []
  #{["/api/ping" :post (conj intercept/common-interceptors `pong-post)]
    ["/api/ping" :get (conj intercept/common-interceptors `pong)]})
