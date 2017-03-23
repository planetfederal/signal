(ns signal.components.http.store
  (:require [signal.components.http.intercept :as intercept]
            [clj-http.client :as client]
            [signal.components.http.response :as response]
            [clojure.xml :as xml]
            [clojure.spec :as s]
            [clojure.tools.logging :as log]
            [signal.entity.scmessage :as scm]
            [signal.components.store.core :as storeapi])
  (:import (com.boundlessgeo.spatialconnect.schema SCCommand)))

(defn http-get-store
  "Gets a store by id"
  [store-comp request]
  (log/debug "Getting store by id")
  (let [id (get-in request [:path-params :id])]
    (if-let [store (storeapi/find-by-id store-comp id)]
      (response/ok store)
      (let [err-msg (str "No store found for id" id)]
        (log/warn err-msg)
        (response/not-found err-msg)))))

(defn http-put-store
  "Updates a store using the json body then publishes
  a config update message about the newly updated store"
  [store-comp request]
  (log/debug "Updating store")
  (let [store (:json-params request)
        id (get-in request [:path-params :id])]
    (if (s/valid? :signal.specs.store/store-spec store)
      (let [updated-store (storeapi/modify store-comp id store)]
        (response/ok updated-store))
      (let [err-msg "Failed to update store"]
        (log/error (str err-msg "b/c" (s/explain-str :signal.specs.store/store-spec store)))
        (response/bad-request err-msg)))))

(defn http-post-store
  "Creates a new store using the json body then publishes
  a config update message about the newly updated store"
  [store-comp request]
  (let [store (:json-params request)]
    (log/debug "Validating store")
    (if (s/valid? :signal.specs.store/store-spec store)
      (let [new-store (storeapi/create store-comp store)]
        (log/debug "Added new store")
          (response/ok new-store))
      (let [err-msg "Failed to create new store"]
        (log/error (str err-msg "b/c" (s/explain-str :signal.specs.store/store-spec store)))
        (response/bad-request err-msg)))))

(defn http-delete-store
  "Deletes a store by id then publishes a config update message about
  the delted store"
  [store-comp request]
  (log/debug "Deleting store")
  (let [id (get-in request [:path-params :id])
        store (storeapi/find-by-id store-comp id)]
    (if (nil? store)
      (let [err-msg (str "No store found with id" id)]
        (log/error err-msg)
        (response/bad-request err-msg))
      (do
        (storeapi/delete store-comp id)
        (response/ok "success")))))

(defn http-get-all-stores
  "Returns http response of all stores"
  [store-comp _]
  (log/debug "Getting all stores")
  (response/ok (storeapi/all store-comp)))

(defn get-capabilities->layer-names
  "Takes a WFS GetCapabilities document as an xml string and returns
  a list of layer names specified in the document"
  [caps]
  (let [layer-names (->> caps
                         .getBytes
                         java.io.ByteArrayInputStream.
                         xml/parse
                         :content
                         (filter #(= :FeatureTypeList (:tag %)))
                         first
                         :content
                         (filter #(= :FeatureType (:tag %)))
                         (map #(first (:content (first (:content %))))))]
    layer-names))

(defn http-get-capabilities
  "Makes a request to a WFS endpoint specified by the 'url'
  query parmeter.  Used as a proxy to avoid cross origin issues."
  [request]
  (let [url (get-in request [:query-params :url])
        res (client/get (str url "?service=WFS&version=1.1.0&request=GetCapabilities") {:ignore-unknown-host? true})
        status (:status res)
        body (:body res)]
    (if (= status 200)
      (response/ok (get-capabilities->layer-names body))
      (response/bad-request (str "Could not get capabilities from " url)))))

(defn routes [store-comp]
  #{["/api/stores" :get
     (conj intercept/common-interceptors (partial http-get-all-stores store-comp)) :route-name :http-all-stores]
    ["/api/stores/:id" :get
     (conj intercept/common-interceptors (partial http-get-store store-comp)) :route-name :http-get-store]
    ["/api/stores/:id" :put
     (conj intercept/common-interceptors (partial http-put-store store-comp)) :route-name :http-put-store]
    ["/api/stores" :post
     (conj intercept/common-interceptors (partial http-post-store store-comp)) :route-name :http-post-store]
    ["/api/stores/:id" :delete
     (conj intercept/common-interceptors (partial http-delete-store store-comp)) :route-name :http-delete-store]
    ["/api/wfs/getCapabilities" :get
     (conj intercept/common-interceptors `http-get-capabilities)]})
