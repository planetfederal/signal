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

(ns signal.config
  (:require [com.stuartsierra.component :as component]
            [clojure.data.json :as json]
            [aero.core :refer [read-config]]))

(defn pcf []
  {:resource {:database (or (some-> (System/getenv "VCAP_SERVICES")
                                    (json/read-str :key-fn
                                                   clojure.core/keyword)
                                    :pg_95_XL_DEV_CONTENT_001
                                    first
                                    :credentials
                                    (clojure.set/rename-keys)
                                    {:db_host :host
                                     :db_port :port
                                     :db_name :name})
                            {})}})

(def config (merge (read-config "resources/config-default.edn")
                   (read-config "resources/config-local.edn")))
