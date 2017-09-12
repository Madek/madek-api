(ns madek.api.resources.previews.index
  (:require
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [madek.api.utils.sql :as sql]

    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(defn get-index [media-file]
  (let [query (-> (sql/select :previews.*)
                  (sql/from :previews)
                  (sql/merge-where
                    [:= :previews.media_file_id (:id media-file)])
                  (sql/format))]
    (jdbc/query (rdbms/get-ds) query)))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
