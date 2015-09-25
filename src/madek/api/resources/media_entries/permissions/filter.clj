(ns madek.api.resources.media-entries.permissions.filter
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [drtom.logbug.catcher :as catcher]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.thrown :as thrown]
    [honeysql.sql :refer :all]
    [madek.api.resources.groups.index :as groups]
    ))

(defn- sql-me-permission-for-user
  [sqlmap perm-name perm-bool auth-entity]
  (if perm-bool
    (let [user-id (:id auth-entity)
          group-ids (map :id (groups/get-index {:user-id user-id}))
          newsqlmap (-> sqlmap
                        (sql-merge-left-join [:media_entry_user_permissions :meup]
                                             [:= :me.id :meup.media_entry_id])
                        (sql-merge-where [:or
                                          [:= (keyword (str "me." perm-name)) perm-bool]
                                          [:= :me.responsible_user_id user-id]
                                          [:and
                                           [:= (keyword (str "meup." perm-name)) perm-bool]
                                           [:= :meup.user_id user-id]]]))]
      (cond-> newsqlmap
        (seq group-ids)
        (-> (sql-merge-left-join [:media_entry_group_permissions :megp]
                                 [:= :me.id :megp.media_entry_id])
            (update-in [:where]
                       conj [:and
                             [:= (keyword (str "megp." perm-name)) perm-bool]
                             [:in :megp.group_id group-ids]]))))
    sqlmap))

(defn- sql-me-permission-for-api-client
  [sqlmap perm-name perm-bool {api-client-id :id}]
  (cond-> sqlmap
    perm-bool
    (-> (sql-merge-left-join [:media_entry_api_client_permissions :meacp]
                             [:= :me.id :meacp.media_entry_id])
        (sql-merge-where [:or
                          [:= (keyword (str "me." perm-name)) perm-bool]
                          [:and
                           [:= (keyword (str "meacp." perm-name)) perm-bool]
                           [:= :meacp.api_client_id api-client-id]]]))))

(defn- sql-me-get-metadata-and-previews-for-user
  [sqlmap query-params-with-auth-entity]
  (sql-me-permission-for-user
    sqlmap
    "get_metadata_and_previews"
    (read-string (:me_get_metadata_and_previews query-params-with-auth-entity))
    (:auth-entity query-params-with-auth-entity)))

(defn- sql-me-get-full-size-for-user
  [sqlmap query-params-with-auth-entity]
  (sql-me-permission-for-user
    sqlmap
    "get_full_size"
    (read-string (:me_get_full_size query-params-with-auth-entity))
    (:auth-entity query-params-with-auth-entity)))

(defn- sql-me-get-metadata-and-previews-for-api-client
  [sqlmap query-params-with-auth-entity]
  (sql-me-permission-for-api-client
    sqlmap
    "get_metadata_and_previews"
    (read-string (:me_get_metadata_and_previews query-params-with-auth-entity))
    (:auth-entity query-params-with-auth-entity)))

(defn- sql-me-get-full-size-for-api-client
  [sqlmap query-params-with-auth-entity]
  (sql-me-permission-for-api-client
    sqlmap
    "get_full_size"
    (read-string (:me_get_full_size query-params-with-auth-entity))
    (:auth-entity query-params-with-auth-entity)))

;################################################################

(def ^:private me-permission-dispatch-map
  {:me_get_metadata_and_previews {"User" sql-me-get-metadata-and-previews-for-user
                                  "ApiClient" sql-me-get-metadata-and-previews-for-api-client}
   :me_get_full_size {"User" sql-me-get-full-size-for-user
                      "ApiClient" sql-me-get-full-size-for-api-client}})

(defn sql-me-permission [sqlmap perm-param query-params-with-auth-entity]
  (if-let [perm-value (perm-param query-params-with-auth-entity)]
    (let [auth-entity-type (get-in query-params-with-auth-entity
                                   [:auth-entity :type])
          sql-fn (get-in me-permission-dispatch-map
                         [perm-param auth-entity-type])]
      (sql-fn sqlmap query-params-with-auth-entity))
    sqlmap))

;################################################################

(defn sql-public-get-metadata-and-previews
  [sqlmap {:keys [public_get_metadata_and_previews]}]
  (cond-> sqlmap
    public_get_metadata_and_previews
    (sql-merge-where [:= :me.get_metadata_and_previews
                      (read-string public_get_metadata_and_previews)])))

(defn sql-public-get-full-size
  [sqlmap {:keys [public_get_full_size]}]
  (cond-> sqlmap
    public_get_full_size
    (sql-merge-where [:= :me.get_full_size
                      (read-string public_get_full_size)])))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
