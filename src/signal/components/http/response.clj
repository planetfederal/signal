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

(ns signal.components.http.response
  (:require [camel-snake-kebab.core :refer :all]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [clojure.tools.logging :as log]))

(defn is-error? [status]
  (< 300 status))

(defn make-response
  "Creates a response map using the status, body, and headers."
  [status body & {:as headers}]
  (let [res-body (assoc {}
                        :result (if (is-error? status) nil body)
                        :error (if (is-error? status) body nil))]
    (let [res {:status status :body res-body :headers headers}]
      (log/trace "Returning response" res)
      res)))

(def ok (partial make-response 200))
(def created (partial make-response 201))
(def accepted (partial make-response 202))
(def bad-request (partial make-response 400))
(def unauthorized (partial make-response 401))
(def forbidden (partial make-response 403))
(def not-found (partial make-response 404))
(def conflict (partial make-response 409))
(def error (partial make-response 500))
(def unavailable (partial make-response 503))
