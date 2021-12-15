(ns madek.api.resources.media-resources.permissions
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.utils.sql :as sql]
    ))

(defn- build-api-client-permissions-query
  [media-resource-id api-client-id perm-name & {:keys [mr-type]}]
  (-> (sql/select :*)
      (sql/from (keyword (str mr-type "_api_client_permissions")))
      (sql/where [:= (keyword (str mr-type "_id")) media-resource-id]
                 [:= :api_client_id api-client-id]
                 [:= perm-name true])
      (sql/format)))

(defn- build-user-permissions-query
  [media-resource-id user-id perm-name & {:keys [mr-type]}]
  (-> (sql/select :*)
      (sql/from (keyword (str mr-type "_user_permissions")))
      (sql/where [:= (keyword (str mr-type "_id")) media-resource-id]
                 [:= :user_id user-id]
                 [:= perm-name true])
      (sql/format)))

(defn- build-user-groups-query [user-id]
  (-> (sql/select :groups.*)
      (sql/from :groups)
      (sql/merge-join :groups_users [:= :groups.id :groups_users.group_id])
      (sql/where [:= :groups_users.user_id user-id])
      (sql/format)))

(defn- query-user-groups [user-id]
  (->> (build-user-groups-query user-id)
       (jdbc/query (rdbms/get-ds))))

(defn- build-group-permissions-query
  [media-resource-id group-ids perm-name & {:keys [mr-type]}]
  (-> (sql/select :*)
      (sql/from (keyword (str mr-type "_group_permissions")))
      (sql/where [:= (keyword (str mr-type "_id")) media-resource-id]
                 [:in :group_id group-ids]
                 [:= perm-name true])
      (sql/format)))

; ============================================================

(defn- delegation-ids [user_id]
  (let [query {:union [
                (-> (sql/select :delegation_id)
                    (sql/from :delegations_groups)
                    (sql/where [:in :delegations_groups.group_id (->
                      (sql/select :group_id)
                      (sql/from :groups_users)
                      (sql/where [:= :groups_users.user_id user_id]))]))
                (-> (sql/select :delegation_id)
                    (sql/from :delegations_users)
                    (sql/where [:= :delegations_users.user_id user_id]))]}]
    (map #(:delegation_id %) (jdbc/query (rdbms/get-ds) (sql/format query)))
  )
)

(defn- query-api-client-permissions
  [resource api-client-id perm-name & {:keys [mr-type]}]
  (->> (build-api-client-permissions-query
         (:id resource) api-client-id perm-name :mr-type mr-type)
       (jdbc/query (rdbms/get-ds))))

(defn- query-user-permissions
  [resource user-id perm-name & {:keys [mr-type]}]
  (->> (build-user-permissions-query
         (:id resource) user-id perm-name :mr-type mr-type)
       (jdbc/query (rdbms/get-ds))))

(defn- query-group-permissions
  [resource user-id perm-name & {:keys [mr-type]}]
  (if-let [user-groups (seq (query-user-groups user-id))]
    (->> (build-group-permissions-query
           (:id resource) (map :id user-groups) perm-name :mr-type mr-type)
         (jdbc/query (rdbms/get-ds)))))

(defn permission-by-auth-entity? [resource auth-entity perm-name & {:keys [mr-type]}]
  (or (perm-name resource)
      (let [auth-entity-id (:id auth-entity)]
        (-> (case (:type auth-entity)
              "User" (or (= auth-entity-id (:responsible_user_id resource))
                         (some #(= (:responsible_delegation_id resource) %) (delegation-ids auth-entity-id))
                         (seq (query-user-permissions resource
                                                      auth-entity-id
                                                      perm-name
                                                      :mr-type mr-type))
                         (seq (query-group-permissions resource
                                                       auth-entity-id
                                                       perm-name
                                                       :mr-type mr-type)))
              "ApiClient" (seq (query-api-client-permissions resource
                                                             auth-entity-id
                                                             perm-name
                                                             :mr-type mr-type)))
            boolean))))

(defn viewable-by-auth-entity? [resource auth-entity & {:keys [mr-type]}]
  (permission-by-auth-entity? resource
                              auth-entity
                              :get_metadata_and_previews
                              :mr-type mr-type))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
