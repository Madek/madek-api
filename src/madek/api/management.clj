(ns madek.api.management
  (:require
    [cider-ci.utils.config :as config :refer [get-config]]
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.thrown :as thrown]
    [madek.api.authentication.basic-auth :as basic-auth]
    ))


(defn- shutdown [_]
  (future
    (Thread/sleep 500)
    (System/exit 0))
  {:status 204})

(defn- get-status [_]
  ; TODO possibly check DB connection
  {:body "OK"})

(def ^:private management-handler
  (cpj/routes
    (cpj/GET "/management/status" _ get-status)
    (cpj/ANY "/management/status" _ {:status 405})
    (cpj/POST "/management/shutdown" _ shutdown )
    (cpj/ANY "/management/shutdown" _ {:status 405})
    (cpj/ANY "*" _ {:status 404 :body {:message "404 NOT FOUND"}})))

(defn- handle-management-request [request]
  (if-let [password (-> request basic-auth/extract :password)]
    (if-not (= password (-> (get-config) :madek_master_secret))
      {:status 401 :body "Password doesn't match the madek_master_secret"}
      (management-handler request))
    {:status 401 :body "The management pages require basic password authentication."}))

(defn wrap [default-handler]
  (cpj/routes
    (cpj/ANY "/management*" _ handle-management-request)
    (cpj/ANY "*" _ default-handler)))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

