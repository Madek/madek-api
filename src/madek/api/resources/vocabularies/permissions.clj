(ns madek.api.resources.vocabularies.permissions
  (:require
    [madek.api.authentication.basic :refer [extract]]
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [madek.api.utils.sql :as sql]

    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [logbug.catcher :as catcher]
    ))

(defn- execute-query
  [query]
  (jdbc/query (rdbms/get-ds) query))

(defn- group-ids
  [user-id]
  (if (nil? user-id)
    '()
    (let [query (-> (sql/select :group_id)
                    (sql/modifiers :distinct)
                    (sql/from :groups_users)
                    (sql/where [:= :groups_users.user_id user-id])
                    (sql/format))]
      (map :group_id (execute-query query)))))

(defn- user-permissions-query
  [user-id]
  (if user-id
    (-> (sql/select :vocabulary_id)
        (sql/from :vocabulary_user_permissions)
        (sql/merge-where
          [:= :vocabulary_user_permissions.user_id user-id]
          [:= :vocabulary_user_permissions.view true])
        (sql/format))))

(defn- pluck-vocabulary-ids
  [query]
  (if (nil? query)
    '()
    (map :vocabulary_id (execute-query query))))

(defn- group-permissions-query
  [user-id]
  (let [groups-ids-result (group-ids user-id)]
    (if (empty? groups-ids-result)
      nil
      (-> (sql/select :vocabulary_id)
          (sql/from :vocabulary_group_permissions)
          (sql/where
            [:in :vocabulary_group_permissions.group_id (group-ids user-id)]
            [:= :vocabulary_group_permissions.view true])
          (sql/format)))))

(defn accessible-vocabulary-ids
  [user-id]
  (concat
    (pluck-vocabulary-ids (user-permissions-query user-id))
    (pluck-vocabulary-ids (group-permissions-query user-id))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
