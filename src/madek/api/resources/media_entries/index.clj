(ns madek.api.resources.media-entries.index
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [honeysql.sql :refer :all]
    [madek.api.pagination :as pagination]
    [madek.api.resources.media-entries.permissions.filter :as permissions-sql]
    [madek.api.resources.shared :as shared]
    ))

(defn- sql-collection-id
  [sqlmap {:keys [collection_id] :as query-params-with-auth-entity}]
  (logging/info 'COLLECTION_ID collection_id)
  (cond-> sqlmap
    (seq collection_id)
    (-> (sql-merge-join [:collection_media_entry_arcs :cmea]
                        [:= :cmea.media_entry_id :me.id])
        (sql-merge-where [:= :cmea.collection_id collection_id]))))

(defn build-index-base-query
  [{:keys [order] :or {order :asc} :as query-params-with-auth-entity}]
  (-> (sql-select :me.id, :me.created_at)
      (sql-merge-modifiers :distinct)
      (sql-from [:media-entries :me])
      (sql-collection-id query-params-with-auth-entity)
      (permissions-sql/sql-public-get-metadata-and-previews
        query-params-with-auth-entity)
      (permissions-sql/sql-public-get-full-size
        query-params-with-auth-entity)
      (permissions-sql/sql-me-permission :me_get_metadata_and_previews
                                         query-params-with-auth-entity)
      (permissions-sql/sql-me-permission :me_get_full_size
                                         query-params-with-auth-entity)
      (sql-order-by [:me.created-at (keyword order)])
      (sql-limit pagination/LIMIT)))


(defn- build-query [query-params-with-auth-entity]
  (-> (build-index-base-query query-params-with-auth-entity)
      (pagination/add-offset-for-honeysql query-params-with-auth-entity)
      sql-format))

(defn- query-index-resources [query-params-with-auth-entity]
  (jdbc/query (rdbms/get-ds) (build-query query-params-with-auth-entity)))

(defn- wrap-permissions-params-combination-check [handler query-params]
  (letfn [(permissions-params-combined? [query-params]
            (> (count (select-keys query-params [:public_get_metadata_and_previews
                                                 :public_get_full_size
                                                 :me_get_metadata_and_previews
                                                 :me_get_full_size])) 1))]
    (fn [request]
      (if (permissions-params-combined? query-params)
        {:status 422 :body {:message "It is not allowed to combine multiple permission query parameters!"}}
        (handler request)))))

(defn- wrap-permissions-params-false-value-check [handler query-params]
  (letfn [(me-permissions-params-some-false-value? [query-params]
            (not-every? true?
                        (vals (select-keys query-params
                                           [:me_get_metadata_and_previews
                                            :me_get_full_size]))))]
    (fn [request]
      (if (me-permissions-params-some-false-value? query-params)
        {:status 422 :body {:message "True value must be provided for 'me_' permission parameters"}}
        (handler request)))))

(defn- get-index-base [{:keys [query-params authenticated-entity]}]
  (catcher/wrap-with-log-error
    {:body
     {:media-entries
      (query-index-resources (into query-params
                             {:auth-entity authenticated-entity}))}}))

(defn get-index [request]
  (let [{query-params :query-params} request]
    (-> get-index-base
        (wrap-permissions-params-false-value-check query-params)
        (wrap-permissions-params-combination-check query-params))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
