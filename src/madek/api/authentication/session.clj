(ns madek.api.authentication.session
  (:require
    [cider-ci.open-session.encryptor :refer [decrypt]]
    [cider-ci.open-session.signature :refer [valid?]]
    [cider-ci.utils.config :as config :refer [get-config]]
    [cider-ci.utils.rdbms :as rdbms]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [clojure.walk :refer [keywordize-keys]]
    [drtom.logbug.catcher :as catcher]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.thrown :as thrown]
    ))

(defn- get-session-secret []
  (-> (get-config) :madek_master_secret))

(defn get-user [user-id]
  (when-let [user (-> (jdbc/query (rdbms/get-ds)
                                  ["SELECT * FROM users WHERE id = ? " user-id])
                      first)]
    (assoc user :type "User")))


(defn get-madek-session-cookie-name []
  (or (-> (get-config) :madek_session_cookie_name keyword)
      (throw (IllegalStateException. (" madek_session_cookie_name is not configured")))))


(defn- handle [request handler]
  (logging/info 'session/handle [request handler])
  (if-let [cookie-value (-> request keywordize-keys :cookies (get (get-madek-session-cookie-name)) :value)]
    (let [session-object (decrypt (get-session-secret) cookie-value)]
      (logging/info 'session/handle {:cookie-value cookie-value})
      (logging/info 'session/handle {:session-object session-object})
      (if-let [user (-> session-object :user_id get-user)]
        ; TODO check expiration in session-object
        (if (valid? (-> session-object :signature)
                    (get-session-secret)
                    (-> user :password_digest))
          (handler (assoc request
                          :authenticated-entity user
                          :authentication-method "Session"))
          {:status 401 :body "The session is invalid."})
        {:status 401 :body "The user was not found."}))
    (handler request)))

(defn wrap [handler]
  (fn [request]
    (handle request handler)))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
