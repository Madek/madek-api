(ns madek.api.authentication.session
  (:require
    [clj-time.core :as time]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [clojure.walk :refer [keywordize-keys]]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [madek.api.utils.config :refer [get-config parse-config-duration-to-seconds]]
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.utils.sql :as sql]
    [taoensso.timbre :refer [debug info warn error spy]]
    ))


(defn- get-cookie-value [request]
  (-> request keywordize-keys :cookies
      spy (get :madek-session) spy :value))

(defn find-valid-user-session-sql [session-token]
  (-> (sql/select :users.*)
      (sql/from :users)
      (sql/merge-join :user_sessions [:= :users.id :user_id])
      (sql/merge-select
        [:user_sessions.id :user_session_id]
        [:user_sessions.created_at :user_session_created_at]
        [:auth_systems.external_sign_out_url :external_sign_out_url])
      (sql/merge-join :auth_systems
                      [:= :auth_systems.id
                       :user_sessions.auth_system_id])
      (sql/merge-where (sql/call
                         := :user_sessions.token_hash
                         (sql/call :encode
                                   (sql/call :digest session-token "sha256") 
                                   "base64")))
      (sql/merge-where
        (sql/raw (str "now() < user_sessions.created_at + "
                      "auth_systems.session_max_lifetime_hours "
                      "* interval '1 hour'")))))

(defn find-valid-user-session [cookie-value]
  (some-> cookie-value
          find-valid-user-session-sql
          sql/format
          (->> (jdbc/query (rdbms/get-ds)) first)
          (assoc :type "User")))

(defn- handle [{:as request} handler]
  (if-let [cookie-value (get-cookie-value request)]
    (if-let [user-session (find-valid-user-session cookie-value)]
      (handler (assoc request
                      :authenticated-entity user-session
                      :authentication-method "Session"))
      {:status 401 :body "No valid session found."})
    (handler request)))

(defn wrap [handler]
  (fn [request]
    (handle request handler)))

;### Debug ####################################################################
(debug/debug-ns *ns*)
