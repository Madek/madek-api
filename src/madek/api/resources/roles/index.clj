(ns madek.api.resources.roles.index
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.pagination :as pagination]
   [madek.api.utils.rdbms :as rdbms]
   [madek.api.utils.sql :as sql]))

(defn- query
  [query-params]
  (-> (sql/select :roles.*)
      (sql/from :roles)
      (sql/join :meta_keys [:= :roles.meta_key_id :meta_keys.id])
      (sql/order-by [:meta_keys.position :asc]
                    [:roles.id :asc])
      (pagination/add-offset-for-honeysql query-params)
      (sql/format)))

(defn get-index
  [request]
  (let [query-params (-> request :query-params)]
    {:body
     {:roles
      (jdbc/query (rdbms/get-ds) (query query-params))}}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
