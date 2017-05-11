(ns madek.api.authentication.basic
  (:require
    [camel-snake-kebab.core :refer :all]
    [cider-ci.open-session.bcrypt :refer [checkpw]]
    [madek.api.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.data.codec.base64 :as base64]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [inflections.core :refer :all]
    ))

(defn- get-by-login [table-name login]
  (->> (jdbc/query (rdbms/get-ds)
                   [(str "SELECT * FROM " table-name " WHERE login = ?") login])
       (map #(assoc % :type (-> table-name ->PascalCase singular)))
       first))

(defn get-entity-by-login [login]
  (or (get-by-login "api_clients" login)
      (get-by-login "users" login)))

(defn- decode-base64
  [^String string]
  (apply str (map char (base64/decode (.getBytes string)))))

(defn extract [request]
  (logging/debug 'extract request)
  (try (when-let [auth-header (-> request :headers :authorization)]
         (let [decoded-val (decode-base64 (last (re-find #"^Basic (.*)$" auth-header)))
               [username password] (clojure.string/split (str decoded-val) #":" 2)]
           {:username username :password password}))
       (catch Exception _
         (logging/warn "failed to extract basic-auth properties" ))))

(defn handle [request handler]
  "Authenticate with the following rules:
  * carry on of there is no auth header with request as is,
  * return 401 if there is a login but we don't find id in DB,
  * return 401 if there is a login and entity but the password doesn't match,
  * carry on by adding :authenticated-entity to the request."
  (let [{login :username password :password} (extract request)]
    (if-not login
      (handler request); carry on without authenticated entity
      (if-let [authenticated-entity (get-entity-by-login login)]
        (if-not (checkpw password (:password_digest authenticated-entity)); if there is an entity the password must match
          {:status 401 :body (str "Password mismatch for " {:login login})}
          (handler (assoc request
                          :authenticated-entity authenticated-entity
                          :authentication-method "Basic Authentication"
                          )))
        {:status 401 :body (str "Neither User nor ApiClient exists for " {:login login})}))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
