(ns madek.api.resources.groups.index
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [honeysql.sql :refer :all]
    ))

(defn- build-index-base-query []
  (-> (sql-select :groups.id)
      (sql-from :groups)))

(defn- build-index-query [{:keys [user-id]}]
  (cond-> (build-index-base-query)
    user-id
    (-> (sql-merge-join :groups_users
                        [:= :groups.id :groups_users.group_id])
        (sql-merge-where [:= :groups_users.user_id user-id]))))

(defn get-index [query-params]
  (let [query (-> (build-index-query query-params)
                  (pagination/add-offset-for-honeysql query-params)
                  sql-format)]
    (jdbc/query (rdbms/get-ds) query)))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
