(defproject signal "0.8.0-SNAPSHOT"
  :description "Signal Server"
  :url "http://github.com/boundlessgeo/signal"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/data.json "0.2.6"]
                 [io.pedestal/pedestal.service "0.5.1"]
                 [io.pedestal/pedestal.jetty "0.5.1"]
                 [ragtime "0.5.3"]
                 [yesql "0.5.2"]
                 [cljfmt "0.5.1"]
                 [org.postgresql/postgresql "9.4-1201-jdbc4"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.1"]
                 [listora/uuid "0.1.2"]
                 [ch.qos.logback/logback-classic "1.1.7"
                  :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.21"]
                 [org.slf4j/jcl-over-slf4j "1.7.21"]
                 [org.slf4j/log4j-over-slf4j "1.7.21"]
                 [com.stuartsierra/component "0.3.1"]
                 [clojurewerkz/machine_head "1.0.0-beta9"]
                 [com.boundlessgeo.spatialconnect/schema "0.7"]
                 ; todo: the protobuf dependency should be packaged into the schema artifact
                 [com.google.protobuf/protobuf-java "3.1.0"]
                 [buddy "1.1.0"]
                 [camel-snake-kebab "0.4.0"]
                 [org.clojars.diogok/cljts "0.5.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [overtone/at-at "1.2.0"]
                 [clj-http "2.3.0"]
                 [com.gfredericks/test.chuck "0.2.7"]
                 [jonase/eastwood "0.2.3" :exclusions [org.clojure/clojure]]
                 [com.draines/postal "2.0.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.kafka/kafka-clients "0.10.1.1"]
                 [org.apache.kafka/kafka-streams "0.10.0.0-cp1" :exclusions [org.slf4j/slf4j-log4j12]]
                 [clj-time "0.13.0"]
                 [ymilky/franzy "0.0.1"]]

  :repositories  [["osgeo" "http://download.osgeo.org/webdav/geotools/"]
                  ["boundlessgeo-releases" "https://repo.boundlessgeo.com/artifactory/release/"]
                  ["clojars" {:sign-releases false}]
                  ["confluent" {:url "http://packages.confluent.io/maven/"}]
                  ["project" "file:repo"]]
  :dev-dependencies [[lein-reload "1.0.0"]]

  :plugins [[lein-environ "1.0.3"]
            [lein-cljfmt "0.5.6"]
            [ragtime/ragtime.lein "0.3.6"]
            [jonase/eastwood "0.2.3"]
            [lein-codox "0.10.2"]
            [lein-cloverage "1.0.9"]]

  :aliases {"migrate" ["run" "-m" "signal.db.conn/migrate"]
            "rollback" ["run" "-m" "signal.db.conn/rollback"]
            "sampledata" ["run" "-m" "signal.generate-data"]}

  :monkeypatch-clojure-test false
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  ;; If you use HTTP/2 or ALPN, use the java-agent to pull in the correct alpn-boot dependency
  ;:java-agents [[org.mortbay.jetty.alpn/jetty-alpn-agent "2.0.3"]]
  :profiles {:dev {:source-paths ["dev"]
                   :resource-paths ["config", "resources"]
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.1"]
                                  [org.clojure/test.check "0.9.0"]]
                   :plugins [[test2junit "1.2.2"]]}
             :uberjar {:aot :all
                       :dependencies [[org.clojure/test.check "0.9.0"]]}}
  :test2junit-output-dir "target/test-results"
  :test2junit-run-ant true
  :uberjar-name "signal-server.jar"
  :main ^{:skip-aot true} signal.server)
