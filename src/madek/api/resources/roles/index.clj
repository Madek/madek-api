(ns madek.api.resources.roles.index
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.utils.sql :as sql]
    ))

(defn- query
  [query-params]
  (-> (sql/select :roles.*)
      (sql/from :roles)
      (pagination/add-offset-for-honeysql query-params)
      (sql/format)))

(defn get-index
  [request]
  (let [query-params (-> request :query-params)]
    {:body
      {:roles
        (jdbc/query (rdbms/get-ds) (query query-params))}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
