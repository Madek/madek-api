(ns madek.api.resources.groups.users
  (:require
   [clj-uuid]
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.debug :as debug]
   [madek.api.constants :refer [presence]]
   [madek.api.pagination :as pagination]
   [madek.api.pagination :as pagination]
   [madek.api.resources.groups.shared :as groups]
   [madek.api.resources.media-entries.index :refer [get-index]]
   [madek.api.resources.media-entries.media-entry :refer [get-media-entry]]
   [madek.api.resources.shared :as shared]
   [madek.api.resources.users :as users]
   [madek.api.utils.auth :refer [wrap-authorize-admin!]]
   [madek.api.utils.rdbms :as rdbms]
   [madek.api.utils.sql :as sql]
   [ring.util.codec :refer [url-decode]]))

(defn group-user-query [group-id user-id]
  (-> (users/sql-select)
      (sql/from :users)
      (sql/merge-join :groups_users [:= :users.id :groups_users.user_id])
      (sql/merge-join :groups [:= :groups.id :groups_users.group_id])
      (users/sql-merge-where-id user-id)
      (groups/sql-merge-where-id group-id)
      sql/format))

(defn find-group-user [group-id user-id]
  (->> (group-user-query group-id user-id)
       (jdbc/query (rdbms/get-ds))
       first))

(defn get-group-user [group-id user-id]
  (when-let [user (find-group-user group-id user-id)]
    {:body user}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-user [group-id user-id]
  (if-let [user (find-group-user group-id user-id)]
    {:body user}
    (let [group (groups/find-group group-id)
          user (users/find-user user-id)]
      (if-not (and group user)
        {:status 404}
        (do (jdbc/insert! (rdbms/get-ds)
                          :groups_users {:group_id (:id group)
                                         :user_id (:id user)})
            {:body (find-group-user group-id user-id)})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn remove-user [group-id user-id]
  (when-let [user (find-group-user group-id user-id)]
    (let [group (groups/find-group group-id)]
      (jdbc/delete! (rdbms/get-ds)
                    :groups_users
                    ["group_id = ? AND user_id = ?"
                     (:id group) (:id user)])
      {:status 204})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn group-users-query [group-id request]
  (-> (sql/select :users.id :users.institutional_id :users.email)
      (sql/from :users)
      (sql/merge-join :groups_users [:= :users.id :groups_users.user_id])
      (sql/merge-join :groups [:= :groups.id :groups_users.group_id])
      (sql/order-by [:users.id :asc])
      (groups/sql-merge-where-id group-id)
      (pagination/add-offset-for-honeysql (:query-params request))
      sql/format))

(defn group-users [group-id request]
  (jdbc/query (rdbms/get-ds)
              (group-users-query group-id request)))

(defn get-group-users [group-id request]
  {:body {:users (group-users group-id request)}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn current-group-users-ids [tx group-id]
  (->> (-> (sql/select [:user_id :id])
           (sql/from :groups_users)
           (sql/where [:= :groups_users.group_id group-id])
           sql/format)
       (jdbc/query tx)
       (map :id)
       set))

(defn target-group-users-query [users]
  (-> (sql/select :id)
      (sql/from :users)
      (sql/where [:or
                  [:in :users.id (->> users (map :id) (filter identity))]
                  [:in :users.institutional_id (->> users (map :institutional_id) (filter identity))]
                  [:in :users.email (->> users (map :email) (filter identity))]])
      sql/format))

(defn target-group-users-ids [tx users]
  (->> (target-group-users-query users)
       (jdbc/query tx)
       (map :id)
       set))

(defn update-delete-query [group-id ids]
  (-> (sql/delete-from :groups_users)
      (sql/merge-where [:= :groups_users.group_id group-id])
      (sql/merge-where [:in :groups_users.user_id ids])
      sql/format))

(defn update-insert-query [group-id ids]
  (-> (sql/insert-into :groups_users)
      (sql/columns :group_id :user_id)
      (sql/values (->> ids (map (fn [id] [group-id id]))))
      sql/format))

(defn update-group-users [group-id data]
  (jdbc/with-db-transaction [tx (rdbms/get-ds)]
    (let [current-group-users-ids (current-group-users-ids tx group-id)
          target-group-users-ids (target-group-users-ids tx (:users data))]
      (jdbc/execute!
       tx
       (update-delete-query
        group-id (clojure.set/difference current-group-users-ids target-group-users-ids)))
      (jdbc/execute!
       tx
       (update-insert-query
        group-id (clojure.set/difference target-group-users-ids current-group-users-ids)))
      {:status 204})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def routes
  (cpj/routes
   (cpj/GET "/groups/:group-id/users/:user-id"
     [group-id user-id]
     (get-group-user group-id user-id))
   (cpj/PUT "/groups/:group-id/users/:user-id"
     [group-id user-id]
     (add-user group-id user-id))
   (cpj/DELETE "/groups/:group-id/users/:user-id"
     [group-id user-id]
     (remove-user group-id user-id))
   (cpj/GET "/groups/:group-id/users/"
     [group-id :as request]
     (get-group-users group-id request))
   (cpj/PUT "/groups/:group-id/users/"
     [group-id :as {data :body}]
     (update-group-users group-id data))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
