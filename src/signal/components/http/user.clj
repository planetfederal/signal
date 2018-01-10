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

(ns signal.components.http.user
  (:require [signal.components.http.intercept :as intercept]
            [clojure.tools.logging :as log]
            [clojure.spec.alpha :as s]
            [signal.specs.user]
            [signal.components.http.auth :refer [check-auth]]
            [signal.components.user :as userapi]
            [signal.components.http.response :as response]))

(defn http-get-all-users
  "Returns http response for all users"
  [user-comp _]
  (log/debug "Getting all users")
  (response/ok (userapi/all user-comp)))

(defn http-post-user
  "Creates a new user"
  [user-comp request]
  (log/debug "Validating user")
  (let [user (:json-params request)]
    (if (s/valid? :signal.specs.user/user-spec user)
      (if-let [new-user (userapi/create user-comp user)]
        (response/ok new-user))
      (let [reason  (s/explain-str :signal.specs.user/user-spec user)
            err-msg (format "Failed to create new user %s because %s" user reason)]
        (log/error err-msg)
        (response/error err-msg)))))

(defn routes [user-comp]
  #{["/api/users" :get  (conj intercept/common-interceptors check-auth
                              (partial http-get-all-users user-comp))
     :route-name :get-users]
    ["/api/users" :post (conj intercept/common-interceptors check-auth
                              (partial http-post-user user-comp))
     :route-name :post-user]})
