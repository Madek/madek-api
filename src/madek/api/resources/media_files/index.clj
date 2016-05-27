(ns madek.api.resources.media-files.index
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [honeysql.sql :refer :all]
    [madek.api.pagination :as pagination]
    )

  (:import
    [madek.api WebstackException]
    )
  )

;### query ####################################################################

(def ^:private base-query
  (-> (sql-select :id, :created_at)
      (sql-from :media-files)
      (sql-order-by [:created-at :asc])))

(defn- build-query [request]
  (let [query-params (:query-params request)
        media-entry-id (-> request :params :media_entry_id)]
    (-> base-query
        (cond-> media-entry-id
          (sql-merge-where [:= :media_entry_id media-entry-id]))
        (pagination/add-offset-for-honeysql query-params)
        sql-format
        )))

(defn- query-index-resources [request]
  (jdbc/query (rdbms/get-ds) (build-query request)))

;### index ####################################################################

(defn get-index [request]
  (catcher/with-logging {}
    {:body
     {:media-files
      (query-index-resources request)}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'build-query)
