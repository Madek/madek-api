(ns madek.api.resources.meta-keys.meta-key
  (:require
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.debug :as debug]
    ))

(defn build-meta-key-query [id]
  (-> (sql-select :*)
      (sql-from :meta-keys)
      (sql-merge-where
        [:= :meta-keys.id id])
      (sql-format)))

(defn get-meta-key [request]
  (let [id (-> request :params :id)
        query (build-meta-key-query id)]
    (if (re-find #"^[a-z0-9\-\_\:]+:[a-z0-9\-\_\:]+$" id)
      (if-let [meta-key (first
                          (jdbc/query (rdbms/get-ds) query))]
        {:body (select-keys meta-key [:id
                                      :description
                                      :label
                                      :vocabulary_id])}
        {:status 404 :body {:message "Meta-Key could not be found!"}})
      {:status 422
       :body {:message "Wrong meta_key_id format! See documentation."}})))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)


