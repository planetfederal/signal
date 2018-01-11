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
