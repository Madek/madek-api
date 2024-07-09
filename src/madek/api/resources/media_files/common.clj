(ns madek.api.resources.media-files.common
  (:require
    [madek.api.utils.sql :as sql]))


(defn media-entry-undeleted-exists-cond [media-file-id]
  [:exists
   (-> (sql/select 1)
       (sql/from :media_files)
       (sql/merge-where [:= :media_files.id media-file-id])
       (sql/join :media_entries [:= :media_files.media_entry_id :media_entries.id])
       (sql/merge-where [:or [:= :media_entries.deleted_at nil]
                         [:>= :media_entries.deleted_at (sql/raw "now()")]]))])



;### Debug ####################################################################
;(debug/debug-ns *ns*)
