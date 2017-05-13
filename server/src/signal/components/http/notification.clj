(ns signal.components.http.notification
  (:require [signal.components.http.response :as response]
            [signal.components.http.intercept :as intercept]
            [signal.components.notification :as notifapi]))

(defn http-get-notif [notif-comp context]
  (let [id (Integer/parseInt (get-in context [:path-params :id]))]
    (response/ok (notifapi/find-notif-by-id notif-comp id))))

(defn routes [notif-comp]
  #{["/api/notifications/:id" :get (conj intercept/common-interceptors (partial http-get-notif notif-comp)) :route-name :get-notif]})
