(ns signal.output.test)

(ns signal.output.email
  (:require [signal.output.protocol :as proto]
            [postal.core :as postal]
            [signal.components.database :as db]))

(def identifier :test)

(defn- send!
  [email-output message]
  (let [recipients (do (zipmap (:notif-ids message) (:addresses email-output)))]
    (doall (map (fn [[id recipient]]
                  (email-recipient recipient (assoc message :body (build-notification-link id)))
                  (db/mark-as-sent id))
                recipients))))

(defrecord Test [output-fn]
  proto/IOutput
  (recipients [_])
  (send! [_ message]
    (output-fn message)))

(defmethod proto/make-output identifier
  [cfg]
  (->Test (:output-fn cfg)))

