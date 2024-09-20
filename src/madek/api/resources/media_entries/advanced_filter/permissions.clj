(ns madek.api.resources.media-entries.advanced-filter.permissions
  (:require
   [clojure.tools.logging :as logging]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [madek.api.utils.sql :as sql]))

; (defn- delegation-ids-subquery [user_id]
;   {:union
;     [
;       (-> (sql/select :delegation_id)
;           (sql/from :delegations_groups)
;           (sql/where [:in :delegations_groups.group_id (->
;             (sql/select :group_id)
;             (sql/from :groups_users)
;             (sql/where [:= :groups_users.user_id user_id])
;           )])
;       )
;       (-> (sql/select :delegation_id)
;           (sql/from :delegations_users)
;           (sql/where [:= :delegations_users.user_id user_id])
;       )
;     ]
;   }
; )

(defn- api-client-authorized-condition [perm id]
  [:or
   [:= (keyword (str "media_entries." perm)) true]
   [:exists (-> (sql/select true)
                (sql/from [:media_entry_api_client_permissions :meacp])
                (sql/merge-where [:= :meacp.media_entry_id :media_entries.id])
                (sql/merge-where [:= (keyword (str "meacp." perm)) true])
                (sql/merge-where [:= :meacp.api_client_id id]))]])

(defn- group-permission-exists-condition [perm id]
  [:exists (-> (sql/select true)
               (sql/from [:media_entry_group_permissions :megp])
               (sql/merge-where [:= :megp.media_entry_id :media_entries.id])
               (sql/merge-where [:= (keyword (str "megp." perm)) true])
               (sql/merge-where [:= :megp.group_id id]))])

(defn- user-permission-exists-condition [perm id]
  [:exists (-> (sql/select true)
               (sql/from [:media_entry_user_permissions :meup])
               (sql/merge-where [:= :meup.media_entry_id :media_entries.id])
               (sql/merge-where [:= (keyword (str "meup." perm)) true])
               (sql/merge-where [:= :meup.user_id id]))])

(defn- group-permission-for-user-exists-condition [perm id]
  [:exists (-> (sql/select true)
               (sql/from [:media_entry_group_permissions :megp])
               (sql/merge-where [:= :megp.media_entry_id :media_entries.id])
               (sql/merge-where [:= (keyword (str "megp." perm)) true])
               (sql/merge-join :groups
                               [:= :groups.id :megp.group_id])
               (sql/merge-join [:groups_users :gu]
                               [:= :gu.group_id :groups.id])
               (sql/merge-where [:= :gu.user_id id]))])

(defn- user-authorized-condition [perm id]
  ; (println (sql/format (delegation-ids-subquery id)))
  [:or
   [:= (keyword (str "media_entries." perm)) true]
   [:= :media_entries.responsible_user_id id]
   ; [:in :media_entries.responsible_delegation_id (delegation-ids-subquery id)]
   (user-permission-exists-condition perm id)
   (group-permission-for-user-exists-condition perm id)])

(defn- filter-by-permission-for-auth-entity [sqlmap permission authenticated-entity]
  (case (:type authenticated-entity)
    "User" (sql/merge-where sqlmap (user-authorized-condition
                                    permission (:id authenticated-entity)))
    "ApiClient" (sql/merge-where sqlmap (api-client-authorized-condition
                                         permission (:id authenticated-entity)))
    (throw (ex-info (str "Filtering for " permission " requires a signed-in entity.")
                    {:status 422}))))

(defn filter-by-query-params [sqlmap query-params authenticated-entity]

  (doseq [true_param ["me_get_full_size" "me_get_metadata_and_previews"]]
    (when (contains? query-params (keyword true_param))
      (when (not= (get query-params (keyword true_param)) true)
        (throw (ex-info (str "Value of " true_param " must be true when present.")
                        {:status 422})))))

  (cond-> sqlmap

    (:public_get_metadata_and_previews query-params)
    (sql/merge-where [:= :media_entries.get_metadata_and_previews true])

    (:public_get_full_size query-params)
    (sql/merge-where [:= :media_entries.get_full_size true])

    (= (:me_get_full_size query-params) true)
    (filter-by-permission-for-auth-entity "get_full_size" authenticated-entity)

    (= (:me_get_metadata_and_previews query-params) true)
    (filter-by-permission-for-auth-entity "get_metadata_and_previews" authenticated-entity)))

(defn- sql-merge-where-permission-spec [sqlmap permission-spec]
  (case (:key permission-spec)
    "public"
    (-> sqlmap
        (sql/merge-where
         [:=
          :media_entries.get_metadata_and_previews
          (case (:value permission-spec)
            "true" true
            "false" false
            :else (throw
                   (ex-info
                    (str "Invalid filter for \"public\" permission!")
                    {:status 422})))]))

    "responsible_user"
    (-> sqlmap
        (sql/merge-where [:=
                          :media_entries.responsible_user_id
                          (:value permission-spec)]))

    "entrusted_to_user"
    (-> sqlmap
        (sql/merge-where
         [:or
          (user-permission-exists-condition "get_metadata_and_previews"
                                            (:value permission-spec))
          (group-permission-for-user-exists-condition "get_metadata_and_previews"
                                                      (:value permission-spec))]))

    "entrusted_to_group"
    (-> sqlmap
        (sql/merge-where
         (group-permission-exists-condition "get_metadata_and_previews"
                                            (:value permission-spec))))))

(defn sql-filter-by [sqlmap permission-specs]
  (if-not (empty? permission-specs)
    (reduce sql-merge-where-permission-spec
            sqlmap
            permission-specs)
    sqlmap))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'build-query)
