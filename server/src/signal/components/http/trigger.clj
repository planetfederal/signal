(ns signal.components.http.trigger
  (:require [signal.components.http.response :as response]
            [signal.components.http.intercept :as intercept]
            [cljts.io :as jtsio]
            [signal.components.trigger.core :as triggerapi]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojure.spec :as s]))

(defn http-get-all-triggers
  "Returns http response of all triggers"
  [trigger-comp _]
  (log/debug "Getting all triggers")
  (response/ok (triggerapi/all trigger-comp)))

(defn http-get-trigger
  "Gets a trigger by id"
  [trigger-comp request]
  (log/debug "Getting trigger by id")
  (let [id (get-in request [:path-params :id])]
    (if-let [trigger (triggerapi/find-by-id trigger-comp id)]
      (response/ok trigger)
      (let [err-msg (str "No trigger found for id" id)]
        (log/warn err-msg)
        (response/ok err-msg)))))

(defn http-put-trigger
  "Updates a trigger using the json body"
  [trigger-comp request]
  (log/debug "Updating trigger")
  (let [t (:json-params request)]
    (log/debug "Validating trigger")
    (if (s/valid? :signal.specs.trigger/trigger-spec t)
      (let [trigger (triggerapi/modify trigger-comp (get-in request [:path-params :id]) t)]
        (response/ok trigger))
      (let [reason (s/explain-str :signal.specs.trigger/trigger-spec t)]
        (log/error "Failed to update trigger b/c" reason)
        (response/error (str "Failed to update trigger b/c" reason))))))

(defn http-post-trigger
  "Creates a new trigger using the json body"
  [trigger-comp request]
  (log/debug "Adding new trigger")
  (let [t (:json-params request)]
    (log/debug "Validating trigger")
    (if (s/valid? :signal.specs.trigger/trigger-spec t)
      (let [trigger (triggerapi/create trigger-comp t)]
        (response/ok trigger))
      (let [reason (s/explain-str :signal.specs.trigger/trigger-spec t)]
        (log/error "Failed to create trigger b/c" reason)
        (response/error (str "Failed to create trigger b/c" reason))))))

(defn http-delete-trigger
  "Deletes a trigger"
  [trigger-comp request]
  (log/debug "Deleting trigger")
  (let [id (get-in request [:path-params :id])]
    (triggerapi/delete trigger-comp id)
    (response/ok "success")))

(defn http-test-trigger
  "HTTP endpoint used to test triggers.  Takes a geojson feature
  in the json body as the feature to test"
  [triggercomp request]
  (triggerapi/test-value triggercomp "http-api"
                         (-> (:json-params request)
                             json/write-str
                             jtsio/read-feature
                             .getDefaultGeometry))
  (response/ok "success"))

(defn routes [triggercomp]
  #{["/api/triggers" :get
     (conj intercept/common-interceptors (partial http-get-all-triggers triggercomp)) :route-name :get-triggers]
    ["/api/triggers/:id" :get
     (conj intercept/common-interceptors (partial http-get-trigger triggercomp)) :route-name :get-trigger]
    ["/api/triggers/:id" :put
     (conj intercept/common-interceptors (partial http-put-trigger triggercomp)) :route-name :put-trigger]
    ["/api/triggers" :post
     (conj intercept/common-interceptors (partial http-post-trigger triggercomp)) :route-name :post-trigger]
    ["/api/triggers/:id" :delete
     (conj intercept/common-interceptors (partial http-delete-trigger triggercomp)) :route-name :delete-trigger]
    ["/api/trigger/check" :post
     (conj intercept/common-interceptors (partial http-test-trigger triggercomp)) :route-name :http-test-trigger]})

