(ns madek.api.resources.keywords.keyword
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.debug :as debug]
    ))

(defn get-keyword [request]
  (let [id (-> request :params :id)
        query (-> (sql-select :*)
                  (sql-from :keywords)
                  (sql-merge-where
                    [:= :keywords.id id])
                  (sql-format))]
    {:body (select-keys (first (jdbc/query (rdbms/get-ds) query))
                        [:id
                         :meta_key_id
                         :term
                         :description
                         :external_uri
                         :rdf_class
                         :creator_id
                         :created_at])}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

