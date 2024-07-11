(ns madek.api.resources.media-files.common
  (:require
    [madek.api.utils.sql :as sql]))


(defn media-entry-undeleted-exists-cond [media-file-id]
  [:exists
   (-> (sql/select 1)
       (sql/from [:media_files :mfs])
       (sql/merge-where [:= :mfs.id media-file-id])
       (sql/join [:media_entries :mes] [:= :mfs.media_entry_id :mes.id])
       (sql/merge-where [:or
                         [:= :mes.deleted_at nil]
                         [:>= :mes.deleted_at (sql/raw "now()")]]))])



;### Debug ####################################################################
;(debug/debug-ns *ns*)
