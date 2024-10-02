(ns madek.api.resources.collections.advanced-filter.permissions
  (:require
   [clojure.tools.logging :as logging]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [madek.api.utils.sql :as sql]))

(defn- api-client-authorized-condition [perm id]
  [:or
   [:= (keyword (str "collections." perm)) true]
   [:exists (-> (sql/select true)
                (sql/from [:collection_api_client_permissions :cacp])
                (sql/merge-where [:= :cacp.collection_id :collections.id])
                (sql/merge-where [:= (keyword (str "cacp." perm)) true])
                (sql/merge-where [:= :cacp.api_client_id id]))]])

(defn- group-permission-exists-condition [perm id]
  [:exists (-> (sql/select true)
               (sql/from [:collection_group_permissions :cgp])
               (sql/merge-where [:= :cgp.collection_id :collections.id])
               (sql/merge-where [:= (keyword (str "cgp." perm)) true])
               (sql/merge-where [:= :cgp.group_id id]))])

(defn- user-permission-exists-condition [perm id]
  [:exists (-> (sql/select true)
               (sql/from [:collection_user_permissions :cup])
               (sql/merge-where [:= :cup.collection_id :collections.id])
               (sql/merge-where [:= (keyword (str "cup." perm)) true])
               (sql/merge-where [:= :cup.user_id id]))])

(defn- group-permission-for-user-exists-condition [perm id]
  [:exists (-> (sql/select true)
               (sql/from [:collection_group_permissions :cgp])
               (sql/merge-where [:= :cgp.collection_id :collections.id])
               (sql/merge-where [:= (keyword (str "cgp." perm)) true])
               (sql/merge-join :groups
                               [:= :groups.id :cgp.group_id])
               (sql/merge-join [:groups_users :gu]
                               [:= :gu.group_id :groups.id])
               (sql/merge-where [:= :gu.user_id id]))])

(defn- user-authorized-condition [perm id]
  [:or
   [:= (keyword (str "collections." perm)) true]
   [:= :collections.responsible_user_id id]
   (user-permission-exists-condition perm id)
   (group-permission-for-user-exists-condition perm id)])

(defn- filter-by-permission-for-auth-entity [sqlmap authenticated-entity permission]
  (case (:type authenticated-entity)
    "User" (sql/merge-where sqlmap (user-authorized-condition
                                    permission (:id authenticated-entity)))
    "ApiClient" (sql/merge-where sqlmap (api-client-authorized-condition
                                         permission (:id authenticated-entity)))
    (throw (ex-info (str "Filtering for " permission " requires a signed-in entity.")
                    {:status 422}))))

(defn- filter-by-dedicated-permission-for-auth-entity
  [sqlmap {id :id auth-entity-type :type} perm query-param-value]
  (assert (boolean? query-param-value))
  (case auth-entity-type
    "User" (sql/merge-where
            sqlmap
            [:exists (-> (sql/select true)
                         (sql/from [:collection_user_permissions :cup])
                         (sql/merge-where [:= :cup.collection_id :collections.id])
                         (sql/merge-where [:= (keyword (str "cup." perm)) query-param-value])
                         (sql/merge-where [:= :cup.user_id id]))])
    "ApiClient" (sql/merge-where
                 sqlmap
                 [:exists (-> (sql/select true)
                              (sql/from [:collection_api_client_permissions :cacp])
                              (sql/merge-where [:= :cacp.collection_id :collections.id])
                              (sql/merge-where [:= (keyword (str "cacp." perm)) query-param-value])
                              (sql/merge-where [:= :cacp.api_client_id id]))])
    (throw (ex-info (str "Filtering for " perm " requires a signed-in entity.")
                    {:status 422}))))

(defn filter-by-query-params [sqlmap query-params authenticated-entity]
  (doseq [true_param ["me_get_metadata_and_previews"]]
    (when (contains? query-params (keyword true_param))
      (when (not= (get query-params (keyword true_param)) true)
        (throw (ex-info (str "Value of " true_param " must be true when present.")
                        {:status 422})))))
  (let [public_get_metadata_and_previews (:public_get_metadata_and_previews query-params)
        me_get_metadata_and_previews (:me_get_metadata_and_previews query-params)
        me_get_metadata_and_previews_dedicated (:me_get_metadata_and_previews_dedicated query-params)]

    (cond-> sqlmap

      (= public_get_metadata_and_previews true)
      (sql/merge-where [:= :collections.get_metadata_and_previews true])

      (= me_get_metadata_and_previews true)
      (filter-by-permission-for-auth-entity
       authenticated-entity "get_metadata_and_previews")

      (boolean? me_get_metadata_and_previews_dedicated)
      (filter-by-dedicated-permission-for-auth-entity
       authenticated-entity "get_metadata_and_previews"
       me_get_metadata_and_previews_dedicated))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'build-query)
