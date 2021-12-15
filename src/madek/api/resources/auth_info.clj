(ns madek.api.resources.auth-info
  (:require
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    ))


(defn- auth-info [request]
  (if-let [auth-entity (:authenticated-entity request)]
    {:body (merge {}
                  (select-keys auth-entity [:type :id :login :created_at :email_address])
                  (select-keys request [:authentication-method :session-expiration-seconds]))}
    {:status 401}))

(def routes
  (cpj/routes
    (cpj/GET "/auth-info" _ auth-info)))


;### Debug ####################################################################
;(debug/debug-ns *ns*)
