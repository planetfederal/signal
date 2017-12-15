(ns signal.output.email
  (:require [signal.output.protocol :as proto]
            [postal.core :as postal]
            [signal.components.database :as db]))

(def identifier "email")

(def conn {:host (or (System/getenv "SMTP_HOST")
                     "email-smtp.us-east-1.amazonaws.com")
           :ssl  true
           :user (System/getenv "SMTP_USERNAME")
           :pass (System/getenv "SMTP_PASSWORD")})

(defn- build-notification-link
  [id]
  (let [hostname (or (System/getenv "HOSTNAME")
                     (.getHostName (java.net.InetAddress/getLocalHost)))]
    (str "http://" hostname "/notifications/" id)))

(defn email-recipient
  [recipient message]
  (postal/send-message conn {:from    "mobile@boundlessgeo.com"
                             :to      (str recipient)
                             :subject (str (:title message))
                             :body    (str (:body message))}))

(defn- send!
  [email-output message]
  (let [recipients (do (zipmap (:notif-ids message) (:recipients email-output)))]
    (doall (map (fn [[id recipient]]
                  (email-recipient recipient (assoc message :body (str (build-notification-link id)
                                                                       "\n\n"
                                                                       (:body message))))
                  (db/mark-as-sent id))
                recipients))))

(defrecord Email [recipients]
  proto/IOutput
  (recipients [this]
    (:recipients this))
  (send! [this message]
    (send! this message)))

(defmethod proto/make-output identifier
  [cfg]
  (map->Email cfg))

