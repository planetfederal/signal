(ns signal.server
  (:gen-class)                                              ; for -main method in uberjar
  (:require [io.pedestal.http :as server]
            [signal.components.http.core :as http]
            [com.stuartsierra.component :as component]
            [signal.components.trigger :as trigger]
            [signal.components.notification :as notification]
            [clojure.tools.logging :as log]))

(defrecord SignalServer [http-service]
  component/Lifecycle
  (start [component]
    (log/info "Starting SignalServer Component")
    (let [server (server/create-server (:service-def http-service))]
      (server/start server)
      (assoc component :http-server server)))
  (stop [component]
    (log/info "Stopping SignalServer Component")
    (update-in component [:http-server] server/stop)))

(defn new-signal-server []
  (map->SignalServer {}))

(defn make-signal-server
  "Returns a new instance of the system"
  [config-options]
  (log/debug "Making server config with these options" config-options)
  (let [{:keys [http-config]} config-options]
    (component/system-map
     :notify (component/using (notification/make-signal-notification-component) [])
     :trigger (component/using (trigger/make-trigger-component) [:notify])
     :http-service (component/using
                    (http/make-http-service-component http-config)
                    [:trigger :notify])
     :server (component/using (new-signal-server) [:http-service]))))

(defn -main
  "The entry-point for 'lein run'"
  [& _]
  (log/info "Configuring Signal server...")
  (if (= "true" (System/getenv "AUTO_MIGRATE"))
    (signal.db.conn/migrate))
  ;; create global uncaught exception handler so threads don't silently die
  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ thread ex]
       (log/error ex "Uncaught exception on thread" (.getName thread)))))
  (System/setProperty "javax.net.ssl.trustStore"
                      (or (System/getenv "TRUST_STORE")
                          "tls/test-cacerts.jks"))
  (System/setProperty "javax.net.ssl.trustStoreType"
                      (or (System/getenv "TRUST_STORE_TYPE")
                          "JKS"))
  (System/setProperty "javax.net.ssl.trustStorePassword"
                      (or (System/getenv "TRUST_STORE_PASSWORD")
                          "changeit"))
  (System/setProperty "javax.net.ssl.keyStore"
                      (or (System/getenv "KEY_STORE")
                          "tls/test-keystore.p12"))
  (System/setProperty "javax.net.ssl.keyStoreType"
                      (or (System/getenv "KEY_STORE_TYPE")
                          "pkcs12"))
  (System/setProperty "javax.net.ssl.keyStorePassword"
                      (or (System/getenv "KEY_STORE_PASSWORD")
                          "somepass"))
  (component/start-system
   (make-signal-server {:http-config {}})))
