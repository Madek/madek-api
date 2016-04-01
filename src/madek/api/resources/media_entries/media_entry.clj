(ns madek.api.resources.media-entries.media-entry
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.debug :as debug]
    ))

(def ^:private media-entry-keys
  [:id :created_at :responsible_user_id :creator_id :is_published ]
  )

(defn get-media-entry-for-preview [request]
  (let [preview-id (-> request :params :preview_id)
        query (-> (sql-select :*)
                  (sql-from :media_entries)
                  (sql-merge-join :media_files [:= :media_entries.id :media_files.media_entry_id])
                  (sql-merge-join :previews [:= :media_files.id :previews.media_file_id])
                  (sql-merge-where [:= :previews.id preview-id])
                  (sql-format))]
    (first (jdbc/query (rdbms/get-ds) query))))

(defn get-media-entry [request]
  (when-let [media-entry (:media-resource request)]
    {:body (select-keys media-entry media-entry-keys)}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
