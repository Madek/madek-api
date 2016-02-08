(ns madek.api.resources.vocabularies.vocabulary
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.debug :as debug]
    ))

(defn build-vocabulary-query [id]
  (-> (sql-select :*)
      (sql-from :vocabularies)
      (sql-merge-where
        [:= :vocabularies.id id])
      (sql-format)))

(defn get-vocabulary [request]
  (let [id (-> request :params :id)
        query (build-vocabulary-query id)]
    {:body (first (jdbc/query (rdbms/get-ds) query))}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)


