(ns madek.api.authentication.shared
  (:require
   [madek.api.utils.sql :as sql]))

(defn sql-select-user-admin-scopes [query]
  (-> query
      (sql/merge-select
       [(sql/call :exists
                  (-> (sql/select true)
                      (sql/from :admins)
                      (sql/merge-where
                       := :admins.user_id :users.id))) :admin_scope_read]
       [(sql/call :exists
                  (-> (sql/select true)
                      (sql/from :admins)
                      (sql/merge-where
                       := :admins.user_id :users.id))) :admin_scope_write])))
