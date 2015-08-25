(ns madek.api.resources.auth-info
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.debug :as debug]
    ))


(defn- auth-info [request]
  (if-let [auth-entity (:authenticated-entity request)]
    {:body (merge {}
                  (select-keys auth-entity [:type :id :login :created_at])
                  (select-keys request [:authentication-method]))}
    {:status 401}))

(def routes
  (cpj/routes
    (cpj/GET "/auth-info" _ auth-info)))



;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)



