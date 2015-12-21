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
    [madek.api.resources.shared :as shared]
    )

  (:import
    [madek.api WebstackException]
    )
  )

;### permissions ##############################################################

(defn- api-client-authorized-condition [perm id]
  [:or
   [:= (keyword (str "mes." perm)) true]
   [:exists (-> (sql-select 1)
                (sql-from [:media_entry_api_client_permissions :meacp])
                (sql-merge-where [:= :meacp.media_entry_id :mes.id])
                (sql-merge-where [:= (keyword (str "meacp." perm)) true])
                (sql-merge-where [:= :meacp.api_client_id id]))]])

(defn- user-authorized-condition [perm id]
  [:or
   [:= (keyword (str "mes." perm)) true]
   [:= :mes.responsible_user_id id]
   [:exists (-> (sql-select 1)
                (sql-from [:media_entry_user_permissions :meup])
                (sql-merge-where [:= :meup.media_entry_id :mes.id])
                (sql-merge-where [:= (keyword (str "meup." perm)) true])
                (sql-merge-where [:= :meup.user_id id]))]
   [:exists (-> (sql-select 1)
                (sql-from [:media_entry_group_permissions :megp])
                (sql-merge-where [:= :megp.media_entry_id :mes.id])
                (sql-merge-where [:= (keyword (str "megp." perm)) true])
                (sql-merge-join :groups
                                [:= :groups.id :megp.group_id])
                (sql-merge-join [:groups_users :gu]
                                [:= :gu.group_id :groups.id])
                (sql-merge-where [:= :gu.user_id id]))]])


(defn- filter-by-permission-for-auth-entity [sqlmap permission authenticated-entity]
  (case (:type authenticated-entity)
    "User" (sql-merge-where sqlmap (user-authorized-condition
                                     permission (:id authenticated-entity)))
    "ApiClient" (sql-merge-where sqlmap (api-client-authorized-condition
                                          permission (:id authenticated-entity)))
    (throw (WebstackException. (str "Filtering for " permission " requires a signed-in entity." )
                               {:status 422}))))

(defn- filter-by-permissions [sqlmap query-params authenticated-entity]

  (doseq [true_param ["me_get_full_size"  "me_get_metadata_and_previews"]]
    (when (contains? query-params (keyword true_param))
      (when (not= (get query-params (keyword true_param)) true)
        (throw (WebstackException. (str "Value of " true_param " must be true when present." )
                                   {:status 422})))))

  (cond-> sqlmap

    (:public_get_metadata_and_previews query-params)
      (sql-merge-where [:= :mes.get_metadata_and_previews true])

    (:public_get_full_size query-params)
      (sql-merge-where [:= :mes.get_full_size true])

    (= (:me_get_full_size query-params) true)
      (filter-by-permission-for-auth-entity "get_full_size" authenticated-entity)

    (= (:me_get_metadata_and_previews query-params) true)
      (filter-by-permission-for-auth-entity "get_metadata_and_previews" authenticated-entity)))

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
      (sql-limit pagination/LIMIT)
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
        (pagination/add-offset-for-honeysql query-params)
        (filter-by-collection-id query-params)
        (filter-by-permissions query-params authenticated-entity)
        sql-format)))

(defn- query-index-resources [request]
  (jdbc/query (rdbms/get-ds) (build-query request)))


;### index ####################################################################

(defn get-index [request]
  (catcher/wrap-with-log-error
    {:body
     {:media-entries
      (query-index-resources request)}}))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'build-query)
