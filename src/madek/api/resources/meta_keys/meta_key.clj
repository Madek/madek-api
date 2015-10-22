(ns madek.api.resources.meta-keys.meta-key
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.debug :as debug]
    ))

(defn get-meta-key [request]
  (let [id (-> request :params :id)
        query (-> (sql-select :*)
                  (sql-from :meta-keys)
                  (sql-merge-where
                    [:= :meta-keys.id id])
                  (sql-format))]
    {:body (select-keys (first (jdbc/query (rdbms/get-ds) query))
                        [:id
                         :description
                         :label
                         :vocabulary_id])}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)


