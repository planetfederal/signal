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

(ns signal.components.http.auth
  (:require [io.pedestal.interceptor.chain :refer [terminate]]
            [signal.components.http.intercept :as intercept]
            [signal.components.http.response :as response]
            [buddy.hashers :as hashers]
            [buddy.auth.protocols :as proto]
            [buddy.auth.backends :as backends]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :refer [weeks from-now]]
            [signal.components.user.db :as usermodel]
            [clojure.tools.logging :as log]))

(defonce secret "signalsecret")
(def auth-backend (backends/jws {:secret secret}))
(def oauth-backend (backends/jws {:secret secret :token-name "Bearer"}))

(defn get-token
  [user]
  (let [claims {:user user
                :exp  (-> 2 weeks from-now)}]
    ;; todo: encrypt the token
    (jwt/sign claims secret)))

(defn hydrate-token
  [token]
  (jwt/unsign token secret))

(defn token->user [token]
  (:user (hydrate-token token)))

(defn authenticate-user
  "Authenticate user by email and password and return a signed JWT token"
  [req]
  (let [email  (get-in req [:json-params :email])
        pwd    (get-in req [:json-params :password])
        user   (some-> (usermodel/find-by-email {:email email})
                       first)
        authn? (hashers/check pwd (:password user))]
    (log/debug "Autenticating user" user)
    (if-not authn?
      (response/unauthorized "Authentication failed")
      (response/ok {:token (get-token user)}))))

(defn authorize-user
  [request]
  (let [auth-data (try (some->> (proto/-parse oauth-backend request)
                                (proto/-authenticate oauth-backend request))
                       (catch Exception _))]
    (if (:user auth-data)
      (response/ok "User authorized!")
      (response/unauthorized "User not authorized!"))))

(def check-auth
  ;; interceptor to check for Authorization: Token <a token created from get-token>
  {:name :check-auth
   :enter (fn [context]
            (let [request   (:request context)
                  auth-data (try (some->> (proto/-parse auth-backend request)
                                          (proto/-authenticate auth-backend request))
                                 (catch Exception _))]
              (if (:user auth-data)
                (assoc-in context [:request :identity] auth-data)
                (-> context
                    terminate
                    (assoc :response {:status 401
                                      :body {:result nil
                                             :success false
                                             :error "Request failed auth check."}})))))})

(defn routes []
  #{["/api/authenticate" :post (conj intercept/common-interceptors `authenticate-user)]
    ["/api/authorize"    :post (conj intercept/common-interceptors `authorize-user)]})
