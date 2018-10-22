(ns madek.api.resources.roles.role
  (:require
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [madek.api.utils.sql :as sql]
    [logbug.debug :as debug]
    ))

(defn get-role [request]
  (let [id (-> request :params :id)
        query (-> (sql/select :*)
                  (sql/from :roles)
                  (sql/merge-where
                    [:= :roles.id id])
                  (sql/format))]
    {:body (select-keys (first (jdbc/query (rdbms/get-ds) query))
                        [:id
                         :labels
                         :created_at])}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
