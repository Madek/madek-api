(ns madek.api.authentication.session
  (:require
    [cider-ci.open-session.encryptor :refer [decrypt]]
    [cider-ci.open-session.signature :refer [valid?]]
    [madek.api.utils.config :refer [get-config parse-config-duration-to-seconds]]
    [madek.api.utils.rdbms :as rdbms]
    [clj-time.core :as time]
    [clj-time.format :as time-format]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [clojure.walk :refer [keywordize-keys]]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    ))

(defn- get-session-secret []
  (-> (get-config) :madek_master_secret))

(defn- get-user [user-id]
  (when-let [user (-> (jdbc/query (rdbms/get-ds)
                                  ["SELECT * FROM users WHERE id = ? " user-id])
                      first)]
    (assoc user :type "User")))

(defn- get-madek-session-cookie-name []
  (or (-> (get-config) :madek_session_cookie_name keyword)
      (throw (IllegalStateException.
               ("The  madek_session_cookie_name is not configured.")))))

(defn- session-signature-valid? [user session-object]
  (valid? (-> session-object :signature)
          (get-session-secret)
          (-> user :password_digest)))

(defn- decrypt-cookie [cookie-value]
  (catcher/snatch {}
    (decrypt (get-session-secret) cookie-value)))

(defn- get-validity-duration-secs []
  (or (catcher/snatch {}
        ((memoize #(parse-config-duration-to-seconds
                     :madek_session_validity_duration))))
      3))

(defn session-expiration-time [session-object validity-duration-secs]
  (if-let [issued-at (-> session-object :issued_at time-format/parse)]
    (let [valid-for-secs (->> [validity-duration-secs
                               (:max_duration_secs session-object)]
                              (filter identity)
                              (#(if (empty? %) [0] %))
                              (apply min))]
      (time/plus issued-at (time/seconds valid-for-secs)))
    (time/now)))

(defn- session-not-expired? [session-object]
  (when-let [issued-at (-> session-object :issued_at time-format/parse)]
    (time/before? (time/now)
                  (session-expiration-time session-object
                                           (get-validity-duration-secs)))))

(defn- session-enbabled? []
  (-> (get-config) :madek_api_session_enabled boolean))

(defn- get-cookie-value [request]
  (-> request keywordize-keys :cookies
      (get (get-madek-session-cookie-name)) :value))

(defn in-seconds [from to]
  (time/in-seconds (time/interval from to)))

(defn- handle [request handler]
  (if-let [cookie-value (and (session-enbabled?) (get-cookie-value request))]
    (if-let [session-object (decrypt-cookie cookie-value)]
      (if-let [user (-> session-object :user_id get-user)]
        (if-not (session-signature-valid? user session-object)
          {:status 401 :body "The session is invalid!"}
          (let [expiration-time (session-expiration-time
                                  session-object (get-validity-duration-secs))
                now (time/now)]
            (if (time/after? now expiration-time)
              {:status 401 :body "The session has expired!"}
              (handler (assoc request
                              :authenticated-entity user
                              :authentication-method "Session"
                              :session-expiration-seconds
                              (in-seconds now expiration-time))))))
        {:status 401 :body "The user was not found!"})
      {:status 401 :body "Decryption of the session cookie failed!"})
    (handler request)))

(defn wrap [handler]
  (fn [request]
    (handle request handler)))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
