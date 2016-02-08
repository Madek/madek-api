(ns madek.api.resources.vocabularies.index
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clojure.java.jdbc :as jdbc]
    [honeysql.sql :refer :all]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    ))

(def ^:private base-query
  (-> (sql-select :id)
      (sql-from :vocabularies)
      sql-format
      ))

(defn- query-index-resources [request]
  (jdbc/query (rdbms/get-ds) base-query))

(defn get-index [request]
  (catcher/with-logging {}
    {:body
     {:vocabularies
      (query-index-resources request)}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
