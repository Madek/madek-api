(ns madek.api.resources.vocabularies.vocabulary
  (:require
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
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
        [:= :vocabularies.id id]
        [:= :vocabularies.enabled_for_public_view true])
      (sql-format)))

(defn get-vocabulary [request]
  (let [id (-> request :params :id)
        query (build-vocabulary-query id)]
    (if-let [vocabulary (first (jdbc/query (rdbms/get-ds) query))]
      {:body vocabulary}
      {:status 404 :body {:message "Vocabulary could not be found!"}})))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
