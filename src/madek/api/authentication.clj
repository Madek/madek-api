(ns madek.api.authentication
  (:require
    [drtom.logbug.debug :as debug]
    [cider-ci.open-session.bcrypt :refer [checkpw]]
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj :refer [defroutes GET PUT POST DELETE ANY]]
    [drtom.logbug.thrown :as thrown]
    [madek.api.authentication.basic-auth :as basic-auth]
    [camel-snake-kebab.core :refer :all]
    [inflections.core :refer :all]
    ))


(defn- get-entity-by-login [table-name login]
  (->> (jdbc/query (rdbms/get-ds)
                   [(str "SELECT * FROM " table-name " WHERE login = ?") login])
       (map #(assoc % :type (-> table-name ->PascalCase singular)))
       first))


(defn- get-by-login [login]
  (or (get-entity-by-login "api_clients" login)
      (get-entity-by-login "users" login)))

(defn- handle [request handler]
  "Authenticate with the following rules:
  * carry on of there is no auth header with request as is,
  * return 401 if there is a login but we don't find id in DB,
  * return 401 if there is a login and entity but the password doesn't match,
  * carry on by adding :authenticated-entity to the request."
  (let [{login :username password :password} (basic-auth/extract request)]
    (if-not login
      (handler request); carry on without authenticated entity
      (if-let [authenticated-entity (get-by-login login)]
        (if-not (checkpw password (:password_digest authenticated-entity)); if there is an entity the password must match
          {:status 401 :body (str "Password mismatch for " {:login login})}
          (handler (assoc request
                          :authenticated-entity authenticated-entity
                          :authentication-method "Basic Authentication"
                          )))
        {:status 401 :body (str "Neither User nor ApiClient exists for " {:login login})}))))

(defn- add-www-auth-header-if-401 [response]
  (case (:status response)
    401 (assoc-in response [:headers "WWW-Authenticate"] "Basic realm=\"Madek ApiClient or User\"")
    response))

(defn wrap [handler]
  (fn [request]
    (let [response (handle request handler)]
      (add-www-auth-header-if-401 response))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
