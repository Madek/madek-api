(ns madek.api.resources.media-resources.permissions
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [drtom.logbug.thrown :as thrown]
    [drtom.logbug.catcher :as catcher]
    [honeysql.sql :refer :all]
    ))

(defn build-api-client-permissions-query
  [media-resource-id api-client-id & {:keys [mr-type]}]
  (-> (sql-select :*)
      (sql-from (keyword (str mr-type "_api_client_permissions")))
      (sql-where [:= (keyword (str mr-type "_id")) media-resource-id]
                 [:= :api_client_id api-client-id])
      (sql-format)))

(defn build-user-permissions-query
  [media-resource-id user-id & {:keys [mr-type]}]
  (-> (sql-select :*)
      (sql-from (keyword (str mr-type "_user_permissions")))
      (sql-where [:= (keyword (str mr-type "_id")) media-resource-id]
                 [:= :user_id user-id])
      (sql-format)))

(defn- build-user-groups-query [user-id]
  (-> (sql-select :groups.*)
      (sql-from :groups)
      (sql-merge-join :groups_users [:= :groups.id :groups_users.group_id])
      (sql-where [:= :groups_users.user_id user-id])
      (sql-format)))

(defn query-user-groups [user-id]
  (->> (build-user-groups-query user-id)
       (jdbc/query (rdbms/get-ds))))

(defn build-group-permissions-query
  [media-resource-id group-ids & {:keys [mr-type]}]
  (-> (sql-select :*)
      (sql-from (keyword (str mr-type "_group_permissions")))
      (sql-where [:= (keyword (str mr-type "_id")) media-resource-id]
                 [:in :group_id group-ids])
      (sql-format)))

; ============================================================

(defn query-api-client-permissions
  [resource api-client-id & {:keys [mr-type]}]
  (->> (build-api-client-permissions-query
         (:id resource) api-client-id :mr-type mr-type)
       (jdbc/query (rdbms/get-ds))))

(defn query-user-permissions
  [resource user-id & {:keys [mr-type]}]
  (->> (build-user-permissions-query
         (:id resource) user-id :mr-type mr-type)
       (jdbc/query (rdbms/get-ds))))

(defn query-group-permissions
  [resource user-id & {:keys [mr-type]}]
  (if-let [user-groups (seq (query-user-groups user-id))]
    (->> (build-group-permissions-query
           (:id resource) (map :id user-groups) :mr-type mr-type)
         (jdbc/query (rdbms/get-ds)))))

(defn viewable-by-auth-entity [resource auth-entity & {:keys [mr-type]}]
  (let [entity-id (:id auth-entity)]
    (case (:type auth-entity)
      "User" (or (= entity-id (:responsible_user_id resource))
                 (seq (query-user-permissions
                        resource entity-id :mr-type mr-type))
                 (seq (query-group-permissions
                        resource entity-id :mr-type mr-type)))
      "ApiClient" (seq (query-api-client-permissions
                         resource entity-id :mr-type mr-type)))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
