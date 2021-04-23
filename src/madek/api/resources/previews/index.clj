(ns madek.api.resources.previews.index
  (:require
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [madek.api.utils.sql :as sql]

    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(defn- get-first-or-30-percent [list]
  (if (> (count list) 1)
    (nth list (min (Math/ceil (* (/ (count list) 10.0) 3)) (- (count list) 1)))
    (first list)
    ))

(defn- detect-ui-preview-id [sqlmap media-type]
  (if (= media-type "video")
    (let [query (-> sqlmap (sql/merge-where [:= :media_type "image"])
                           (sql/merge-where [:= :thumbnail "large"])
                           (sql/order-by [:previews.filename :asc] [:previews.created_at :desc]))]
      (let [previews (jdbc/query (rdbms/get-ds) (sql/format query))]
        (:id (get-first-or-30-percent previews))))
    nil
    ))

(defn- add-preview-pointer-to [previews detected-id]
  (map #(if (= (:id %) detected-id) (assoc % :used_as_ui_preview true) %) previews))

(defn get-index [media-file]
  (let [sqlmap (-> (sql/select :previews.*)
                   (sql/from :previews)
                   (sql/merge-where
                     [:= :previews.media_file_id (:id media-file)])
                   (sql/order-by [:previews.created_at :desc]))]
    (let [detected-id (detect-ui-preview-id sqlmap (:media_type media-file))]
      (add-preview-pointer-to
        (jdbc/query (rdbms/get-ds) (sql/format sqlmap))
        detected-id))
    ))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
