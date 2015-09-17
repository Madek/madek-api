(ns madek.api.resources.media-entries.index
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.catcher :as catcher]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.ring :refer [wrap-handler-with-logging]]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [honeysql.sql :refer :all]
    ))

(defn build-index-base-query [{order :order}]
  (-> (sql-select :media_entries.id)
      (sql-from :media_entries)
      (sql-order-by [:media_entries.created_at (or (keyword order) :asc)])
      (sql-merge-where  [:= :get_metadata_and_previews true])
      (sql-limit 10)))

(defn- index-resources [query-params]
  (let [query (-> (build-index-base-query query-params)
                  (pagination/add-offset-for-honeysql query-params)
                  sql-format)]
    (logging/info query)
    (jdbc/query (rdbms/get-ds) query)))

(defn get-index [request]
  (catcher/wrap-with-log-error
    {:body {:media-entries (index-resources (:query-params request))}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
