(ns madek.api.resources.roles.role
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
   [madek.api.utils.sql :as sql]))

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
;(debug/debug-ns *ns*)
