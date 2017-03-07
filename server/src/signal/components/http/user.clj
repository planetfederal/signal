(ns signal.components.http.user
  (:require [signal.components.http.intercept :as intercept]
            [clojure.tools.logging :as log]
            [clojure.spec :as s]
            [signal.specs.user]
            [signal.components.user.core :as userapi]
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

(defn http-post-user-team
  "Adds a user to a team"
  [user-comp request]
  (log/debug "Adding user to team")
  (if-let [new-user-team (userapi/add-user-team user-comp (:json-params request))]
    (response/ok new-user-team)
    (let [err-msg "Failed to add user to team"]
      (log/error err-msg)
      (response/error err-msg))))

(defn http-remove-user-team
  "Removes a user from a team"
  [user-comp request]
  (log/debug "Removing user from team")
  (if-let [new-user-team (userapi/remove-user-team user-comp (:json-params request))]
    (response/ok new-user-team)
    (let [err-msg "Failed to remove user from team"]
      (log/error err-msg)
      (response/error err-msg))))

(defn routes [user-comp]
  #{["/api/users" :get  (conj intercept/common-interceptors (partial http-get-all-users user-comp)) :route-name :get-users]
    ["/api/users" :post (conj intercept/common-interceptors (partial http-post-user user-comp)) :route-name :post-user]
    ["/api/user-team" :post (conj intercept/common-interceptors (partial http-post-user-team user-comp)) :route-name :post-user-team]
    ["/api/user-team" :delete (conj intercept/common-interceptors (partial http-remove-user-team user-comp)) :route-name :delete-user-team]})
