(ns madek.api.authentication
  (:require
    [cider-ci.open-session.bcrypt :refer [checkpw]]
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj :refer [defroutes GET PUT POST DELETE ANY]]
    [drtom.logbug.thrown :as thrown]
    [madek.api.authentication.basic-auth :as basic-auth]
    ))


(defn- get-entity-by-login [table-name login]
  (first (jdbc/query
      (rdbms/get-ds) [(str "SELECT * FROM " table-name " WHERE login = ?") login])))

(defn- get-by-login [login]
  (or (get-entity-by-login "api_clients" login)
      (get-entity-by-login "users" login)))

(defn wrap [handler]
  (fn [request]
    (logging/info 'authenticate request)
    (try
      (let [{login :username password :password} (basic-auth/extract request)
            user (get-by-login login) ]
        (when-not user
          (throw (ex-info "User / api-client not found." {})))
        (or (checkpw password (:password_digest user))
            (throw (ex-info "Password does not match." {})))
        (handler (assoc request :authenticated_user user)))
      (catch Exception e
        {:status 401
         :headers {"WWW-Authenticate" "Basic realm= Madek API"}
         :body {:error (str e)}}))))

