(ns madek.api.resources.collections.collection
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(def ^:private collection-keys
  [:id :created_at :responsible_user_id :creator_id :updated_at
   :edit_session_updated_at :meta_data_updated_at])

(defn get-collection [request]
  (when-let [collection (:media-resource request)]
    {:body (select-keys collection collection-keys)}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
