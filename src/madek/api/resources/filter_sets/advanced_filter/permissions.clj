(ns madek.api.resources.filter-sets.advanced-filter.permissions
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [madek.api.utils.sql :as sql])
  (:import
    [madek.api WebstackException]))

(defn- api-client-authorized-condition [perm id]
  [:or
   [:= (keyword (str "filter_sets." perm)) true]
   [:exists (-> (sql/select true)
                (sql/from [:filter_set_api_client_permissions :fsacp])
                (sql/merge-where [:= :fsacp.filter_set_id :filter_sets.id])
                (sql/merge-where [:= (keyword (str "fsacp." perm)) true])
                (sql/merge-where [:= :fsacp.api_client_id id]))]])

(defn- group-permission-exists-condition [perm id]
  [:exists (-> (sql/select true)
               (sql/from [:filter_set_group_permissions :fsgp])
               (sql/merge-where [:= :fsgp.filter_set_id :filter_sets.id])
               (sql/merge-where [:= (keyword (str "fsgp." perm)) true])
               (sql/merge-where [:= :fsgp.group_id id]))])

(defn- user-permission-exists-condition [perm id]
  [:exists (-> (sql/select true)
               (sql/from [:filter_set_user_permissions :fsup])
               (sql/merge-where [:= :fsup.filter_set_id :filter_sets.id])
               (sql/merge-where [:= (keyword (str "fsup." perm)) true])
               (sql/merge-where [:= :fsup.user_id id]))])

(defn- group-permission-for-user-exists-condition [perm id]
  [:exists (-> (sql/select true)
               (sql/from [:filter_set_group_permissions :fsgp])
               (sql/merge-where [:= :fsgp.filter_set_id :filter_sets.id])
               (sql/merge-where [:= (keyword (str "fsgp." perm)) true])
               (sql/merge-join :groups
                               [:= :groups.id :fsgp.group_id])
               (sql/merge-join [:groups_users :gu]
                               [:= :gu.group_id :groups.id])
               (sql/merge-where [:= :gu.user_id id]))])

(defn- user-authorized-condition [perm id]
  [:or
   [:= (keyword (str "filter_sets." perm)) true]
   [:= :filter_sets.responsible_user_id id]
   (user-permission-exists-condition perm id)
   (group-permission-for-user-exists-condition perm id)])

(defn- filter-by-permission-for-auth-entity [sqlmap permission authenticated-entity]
  (case (:type authenticated-entity)
    "User" (sql/merge-where sqlmap (user-authorized-condition
                                     permission (:id authenticated-entity)))
    "ApiClient" (sql/merge-where sqlmap (api-client-authorized-condition
                                          permission (:id authenticated-entity)))
    (throw (WebstackException. (str "Filtering for " permission " requires a signed-in entity." )
                               {:status 422}))))

(defn filter-by-query-params [sqlmap query-params authenticated-entity]

  (doseq [true_param ["me_get_metadata_and_previews"]]
    (when (contains? query-params (keyword true_param))
      (when (not= (get query-params (keyword true_param)) true)
        (throw (WebstackException. (str "Value of " true_param " must be true when present." )
                                   {:status 422})))))

  (cond-> sqlmap

    (:public_get_metadata_and_previews query-params)
      (sql/merge-where [:= :filter_sets.get_metadata_and_previews true])

    (= (:me_get_metadata_and_previews query-params) true)
      (filter-by-permission-for-auth-entity "get_metadata_and_previews" authenticated-entity)))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'build-query)
