(ns madek.api.resources.previews.preview
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [madek.api.constants]
    [madek.api.data-streaming :as data-streaming]
    ))

(defn get-preview [request]
  (let [id (-> request :params :preview_id)
        query (-> (sql-select :*)
                  (sql-from :previews)
                  (sql-merge-where
                    [:= :previews.id id])
                  (sql-format))]
    {:body (first (jdbc/query (rdbms/get-ds) query))}))

(defn- preview-file-path [preview]
  (let [filename (:filename preview)
        [first-char] filename]
    (clojure.string/join
      (java.io.File/separator)
      [madek.api.constants/THUMBNAILS_STORAGE_DIR first-char filename])))

(defn get-preview-file-data-stream [request]
  (catcher/wrap-with-suppress-and-log-warn
    (when-let [preview (:preview request)]
      (when-let [file-path (preview-file-path preview)]
        (data-streaming/respond-with-file file-path
                                          (:content_type preview))))))

;### Debug ####################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)


