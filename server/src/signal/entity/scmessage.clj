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

(ns signal.entity.scmessage
  (:require [clojure.data.json :as json]
            [clojure.string :refer [blank?]]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.tools.logging :as log])
  (:import (com.boundlessgeo.spatialconnect.schema
            SCMessageOuterClass$SCMessage)))

(defn- bytes->map [proto]
  (let [scm (SCMessageOuterClass$SCMessage/parseFrom proto)
        p (.getPayload scm)
        payload (if (blank? p) {} (keywordize-keys (json/read-str p)))]
    (try
      {:correlation-id (.getCorrelationId scm)
       :jwt (.getJwt scm)
       :reply-to (.getReplyTo scm)
       :action (.getAction scm)
       :payload payload}
      (catch Exception e
        (log/error "Could not parse protobuf into map b/c"
                   (.getLocalizedMessage e))))))

(defn- make-protobuf [correlation-id jwt reply-to action payload]
  (-> (SCMessageOuterClass$SCMessage/newBuilder)
      (.setReplyTo reply-to)
      (.setJwt jwt)
      (.setAction action)
      (.setPayload payload)
      (.setCorrelationId correlation-id)
      (.build)))

(defrecord SCMessage [correlation-id jwt reply-to action payload])

(defn from-bytes
  "Deserializes the protobuf byte array into an SCMessage record"
  [b]
  (map->SCMessage (bytes->map b)))

(defn message->bytes
  "Serializes an SCMessage record into a protobuf byte array"
  [message]
  (.toByteArray (make-protobuf
                 (or (get message :correlation-id) -1)
                 (or (get message :jwt) "")
                 (or (get message :reply-to) "")
                 (or (get message :action) -1)
                 (json/write-str (or (get message :payload) "{}")))))
