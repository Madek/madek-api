(ns madek.api.resources.media-entries.media-entry
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [drtom.logbug.debug :as debug]
    ))

(def ^:private media-entry-keys
  [:id :created_at :responsible_user_id :creator_id :is_published ]
  )

(defn get-media-entry [request]
  (when-let [media-entry (:media-resource request)]
    {:body (select-keys media-entry  media-entry-keys)}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
