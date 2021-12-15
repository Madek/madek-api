(ns madek.api.resources.media-entries.media-entry
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.utils.sql :as sql]
    ))

(def ^:private media-entry-keys
  [:id :created_at :responsible_user_id
   :is_published :updated_at :edit_session_updated_at :meta_data_updated_at])

(defn get-media-entry-for-preview [request]
  (let [preview-id (-> request :params :preview_id)
        query (-> (sql/select :*)
                  (sql/from :media_entries)
                  (sql/merge-join :media_files [:= :media_entries.id :media_files.media_entry_id])
                  (sql/merge-join :previews [:= :media_files.id :previews.media_file_id])
                  (sql/merge-where [:= :previews.id preview-id])
                  (sql/format))]
    (first (jdbc/query (rdbms/get-ds) query))))

(defn get-media-entry [request]
  (when-let [media-entry (:media-resource request)]
    {:body (select-keys media-entry media-entry-keys)}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
