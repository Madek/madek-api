(ns madek.api.authentication.session
  (:require
    [cider-ci.open-session.encryptor :refer [decrypt]]
    [cider-ci.open-session.signature :refer [valid?]]
    [cider-ci.utils.config :as config :refer [get-config]]
    [cider-ci.utils.rdbms :as rdbms]
    [clj-time.core :as time]
    [clj-time.format :as time-format]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [clojure.walk :refer [keywordize-keys]]
    [drtom.logbug.catcher :as catcher]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.thrown :as thrown]
    [duckling.core :as duckling]
    ))

(duckling/load!)

(defn- get-session-secret []
  (-> (get-config) :madek_master_secret))

(defn- get-user [user-id]
  (when-let [user (-> (jdbc/query (rdbms/get-ds)
                                  ["SELECT * FROM users WHERE id = ? " user-id])
                      first)]
    (assoc user :type "User")))

(defn- get-madek-session-cookie-name []
  (or (-> (get-config) :madek_session_cookie_name keyword)
      (throw (IllegalStateException. (" madek_session_cookie_name is not configured")))))

(defn- session-signature-valid? [user session-object]
  (valid? (-> session-object :signature)
          (get-session-secret)
          (-> user :password_digest)))

(defn- decrypt-cookie [cookie-value]
  (catcher/wrap-with-suppress-and-log-warn
    (decrypt (get-session-secret) cookie-value)))

(defn- get-validity-duration-secs []
  (or (catcher/wrap-with-suppress-and-log-warn
        ((memoize
           (fn [] (-> (get-config) :madek_session_validity_duration
                      (#(duckling/parse :en$core % [:duration]))
                      first :value :normalized :value)))))
      0))

(defn- session-not-expired? [session-object]
  (when-let [issued-at (-> session-object :issued_at time-format/parse)]
    (when-let [validity-duration-secs (get-validity-duration-secs)]
      (time/before? (time/now)
                    (time/plus issued-at (time/seconds validity-duration-secs))))))

(defn- handle [request handler]
  (if-let [cookie-value (-> request keywordize-keys :cookies
                            (get (get-madek-session-cookie-name)) :value)]
    (if-let [session-object (decrypt-cookie cookie-value)]
      (if-let [user (-> session-object :user_id get-user)]
        (if (session-signature-valid? user session-object)
          (if (session-not-expired? session-object)
            (handler (assoc request
                            :authenticated-entity user
                            :authentication-method "Session"))
            {:status 401 :body "The session has expired!"})
          {:status 401 :body "The session is invalid!"})
        {:status 401 :body "The user was not found!"})
      {:status 401 :body "Decryption of the session cookie failed!"})
    (handler request)))

(defn wrap [handler]
  (fn [request]
    (handle request handler)))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
(debug/debug-ns *ns*)
