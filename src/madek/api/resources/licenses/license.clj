(ns madek.api.resources.licenses.license
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.debug :as debug]
    ))

(defn get-license [request]
  (let [id (-> request :params :id)
        query (-> (sql-select :*)
                  (sql-from :licenses)
                  (sql-merge-where
                    [:= :licenses.id id])
                  (sql-format))]
    {:body (select-keys (first (jdbc/query (rdbms/get-ds) query))
                        [:id
                         :label
                         :usage
                         :url])}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)


