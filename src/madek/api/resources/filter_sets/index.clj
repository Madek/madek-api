(ns madek.api.resources.filter-sets.index
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [madek.api.utils.sql :as sql]
    [madek.api.pagination :as pagination]
    [madek.api.resources.filter-sets.advanced-filter.permissions
     :as permissions :refer [filter-by-query-params]]))

;### collection_id ############################################################

(defn- filter-by-collection-id [sqlmap {:keys [collection_id] :as query-params}]
  (cond-> sqlmap
    (seq collection_id)
    (-> (sql/merge-join [:collection_filter_set_arcs :cfsa]
                        [:= :cfsa.filter_set_id :filter_sets.id])
        (sql/merge-where [:= :cfsa.collection_id collection_id]))))

;### query ####################################################################

(def ^:private base-query
  (-> (sql/select :filter_sets.id, :filter_sets.created_at)
      (sql/from :filter_sets)))

(defn- set-order [query query-params]
  (if (some #{"desc"} [(-> query-params :order)])
    (-> query (sql/order-by [:filter_sets.created-at :desc]))
    (-> query (sql/order-by [:filter_sets.created-at :asc]))))

(defn- build-query [request]
  (let [query-params (:query-params request)
        authenticated-entity (:authenticated-entity request)]
    (-> base-query
        (set-order query-params)
        (filter-by-collection-id query-params)
        (permissions/filter-by-query-params query-params
                                            authenticated-entity)
        (pagination/add-offset-for-honeysql query-params)
        sql/format)))

(defn- query-index-resources [request]
  (jdbc/query (rdbms/get-ds) (build-query request)))

;### index ####################################################################

(defn get-index [request]
  (catcher/with-logging {}
    {:body
     {:filter-sets
      (query-index-resources request)}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'build-query)
