(ns madek.api.resources.collections.collection
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(def ^:private collection-keys
  [:id :created_at :responsible_user_id :updated_at
   :edit_session_updated_at :meta_data_updated_at])

(defn get-collection [request]
  (when-let [collection (:media-resource request)]
    {:body (select-keys collection collection-keys)}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
