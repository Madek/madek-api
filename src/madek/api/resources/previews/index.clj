(ns madek.api.resources.previews.index
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.resources.media-files.common :refer [media-entry-undeleted-exists-cond]]
   [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
   [madek.api.utils.sql :as sql]))

(defn- get-first-or-30-percent [list]
  (if (> (count list) 1)
    (nth list (min (Math/ceil (* (/ (count list) 10.0) 3)) (- (count list) 1)))
    (first list)))

(defn- detect-ui-preview-id [sqlmap media-type]
  (if (= media-type "video")
    (let [query (-> sqlmap (sql/merge-where [:= :media_type "image"])
                    (sql/merge-where [:= :thumbnail "large"])
                    (sql/order-by [:previews.filename :asc] [:previews.created_at :desc]))]
      (let [previews (jdbc/query (rdbms/get-ds) (sql/format query))]
        (:id (get-first-or-30-percent previews))))
    nil))

(defn- add-preview-pointer-to [previews detected-id]
  (map #(if (= (:id %) detected-id) (assoc % :used_as_ui_preview true) %) previews))

(defn previes-query [media-file-id]
  (-> (sql/select :previews.*)
      (sql/from :previews)
      (sql/merge-where
       [:= :previews.media_file_id media-file-id])
      (sql/merge-where (media-entry-undeleted-exists-cond media-file-id))
      (sql/order-by [:previews.created_at :desc])))

(defn get-index [media-file]
  (let [sqlmap (previes-query (:id media-file))]
    (let [detected-id (detect-ui-preview-id sqlmap (:media_type media-file))]
      (add-preview-pointer-to
       (jdbc/query (rdbms/get-ds) (sql/format sqlmap))
       detected-id))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
