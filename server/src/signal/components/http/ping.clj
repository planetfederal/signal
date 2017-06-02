(ns signal.components.http.ping
  (:require [signal.components.http.response :as response]
            [signal.components.http.intercept :as intercept]))

(defn- pong
  "Responds with pong as a way to ensure http service is reachable"
  [_]
  (response/ok "pong"))

(defn routes
  []
  #{["/api/ping" :get (conj intercept/common-interceptors `pong)]})
