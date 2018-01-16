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

(ns signal.components.http.notification
  (:require [signal.components.http.response :as response]
            [signal.components.http.intercept :as intercept]
            [signal.components.notification :as notifapi]
            [signal.components.http.auth :refer [check-auth]]))

(defn http-get-notif [notif-comp context]
  (let [id (Integer/parseInt (get-in context [:path-params :id]))]
    (response/ok (notifapi/find-notif-by-id notif-comp id))))

(defn http-get-notifs [notif-comp _]
  (response/ok (notifapi/all-notifs notif-comp)))

(defn routes [notif-comp]
  #{["/api/notifications/:id" :get (conj intercept/common-interceptors check-auth
                                         (partial http-get-notif notif-comp))
     :route-name :get-notif]
    ["/api/notifications" :get (conj intercept/common-interceptors check-auth
                                     (partial http-get-notifs notif-comp))
     :route-name :get-notifs]})
