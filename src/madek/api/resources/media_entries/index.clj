(ns madek.api.resources.media-entries.index
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [honeysql.sql :refer :all]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [madek.api.resources.media-entries.advanced-filter :as advanced-filter]
    [madek.api.resources.media-entries.advanced-filter.permissions
     :as permissions :refer [filter-by-query-params]]
    )

  (:import
    [madek.api WebstackException]
    )
  )

;### collection_id ############################################################

(defn- filter-by-collection-id [sqlmap {:keys [collection_id] :as query-params}]
  (cond-> sqlmap
    (seq collection_id)
    (-> (sql-merge-join [:collection_media_entry_arcs :cmea]
                        [:= :cmea.media_entry_id :mes.id])
        (sql-merge-where [:= :cmea.collection_id collection_id]))))


;### query ####################################################################

(def ^:private base-query
  (-> (sql-select :mes.id, :mes.created_at)
      (sql-from [:media-entries :mes])
      ))

(defn- set-order [query query-params]
  (if (some #{"desc"} [(-> query-params :order)])
    (-> query (sql-order-by [:mes.created-at :desc]))
    (-> query (sql-order-by [:mes.created-at :asc]))))

(defn- build-query [request]
  (let [query-params (:query-params request)
        authenticated-entity (:authenticated-entity request)]
    (-> base-query
        (set-order query-params)
        (filter-by-collection-id query-params)
        (permissions/filter-by-query-params query-params
                                            authenticated-entity)
        (advanced-filter/filter-by (:filter_by query-params))
        (pagination/add-offset-for-honeysql query-params)
        sql-format
        )))

(defn- query-index-resources [request]
  (jdbc/query (rdbms/get-ds) (build-query request)))


;### index ####################################################################

(defn get-index [request]
  (catcher/with-logging {}
    {:body
     {:media-entries
      (query-index-resources request)}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'build-query)
