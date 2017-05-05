(ns signal.components.http.user
  (:require [signal.components.http.intercept :as intercept]
            [clojure.tools.logging :as log]
            [clojure.spec :as s]
            [signal.specs.user]
            [signal.components.user.user :as userapi]
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
  #{["/api/users" :get  (conj intercept/common-interceptors (partial http-get-all-users user-comp)) :route-name :get-users]
    ["/api/users" :post (conj intercept/common-interceptors (partial http-post-user user-comp)) :route-name :post-user]})
