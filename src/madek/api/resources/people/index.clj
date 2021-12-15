(ns madek.api.resources.people.index
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.utils.sql :as sql]
    ))

(defn- build-index-base-query
  []
  (->
    (sql/select :people.id)
    (sql/from :people)))

(defn- build-index-query
  [{:keys [user-id]}]
  (cond-> (build-index-base-query)
    user-id
      (->
        (sql/merge-join :people_users [:= :people.id :people_users.person_id])
        (sql/merge-where [:= :people_users.user_id user-id]))))

(defn get-index
  [query-params]
  (let [query
          (->
            (build-index-query query-params)
            (pagination/add-offset-for-honeysql query-params)
            sql/format)]
    (jdbc/query (rdbms/get-ds) query)))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
