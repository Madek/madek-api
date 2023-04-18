(ns madek.api.management
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [madek.api.authentication.basic :as basic-auth]
    [madek.api.utils.config :as config :refer [get-config]]
    [madek.api.utils.rdbms :as rdbms]
    ))


(defn- shutdown [_]
  (future
    (Thread/sleep 500)
    (System/exit 0))
  {:status 204})

(defn- get-status [_]
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
;(debug/debug-ns *ns*)
