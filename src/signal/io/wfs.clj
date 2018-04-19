(ns signal.io.wfs
  (:require [signal.io.protocol :as io-proto]
            [clojure.tools.logging :as log]
            [clj-http.client :as http]))

(def identifier "wfs")
(def geoserver-url "http://localhost:8080/geoserver")
(def layername "alerts")

(defn geometry-gml [g]
  (str "<wkb_geometry>"
       "  <gml:Point srsName=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">"
       "    <gml:pos>" (:x g) " " (:y g) "</gml:pos>"
       "  </gml:Point>"
       "</wkb_geometry>"))

(defn payload [geometry]
  (str "<wfs:Transaction service=\"WFS\" version=\"1.0.0\""
       " xmlns:wfs=\"http://www.opengis.net/wfs\""
       " xmlns:signal=\"http://signal\""
       " xmlns:gml=\"http://www.opengis.net/gml\""
       " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
       " xsi:schemaLocation=\"http://www.opengis.net/wfs"
       "                     http://schemas.opengis.net/wfs/1.0.0/WFS-transaction.xsd"
       "                    " geoserver-url "wfs/DescribeFeatureType?typename=signal:"
       "                      " layername  "\">"
       "  <wfs:Insert>"
       "    <signal:" layername  ">"
       "      " (geometry-gml geometry)
       "    </signal:"  layername  ">"
       "  </wfs:Insert>"
       "</wfs:Transaction>"))

(defn post-to-geoserver [payload]
  (http/post (str geoserver-url "ows") {:body payload
                                        :content-type "text/xml"}))

(defn send! [id message]
  (prn identifier id message))

(defrecord WFS [id url]
  io-proto/Output
  (recipients [this]
    (:recipients this))
  (send! [this message]
    (send! this message)))

(defmethod io-proto/make-output identifier
  [cfg]
  (map->WFS cfg))
