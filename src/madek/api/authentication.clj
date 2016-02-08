(ns madek.api.authentication
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [inflections.core :refer :all]
    [madek.api.authentication.basic :as basic-auth]
    [madek.api.authentication.session :as session-auth]
    ))

(defn- add-www-auth-header-if-401 [response]
  (case (:status response)
    401 (assoc-in response [:headers "WWW-Authenticate"] "Basic realm=\"Madek ApiClient or User\"")
    response))

(defn wrap [handler]
  (let [handler (session-auth/wrap handler)]
    (fn [request]
      (let [response (basic-auth/handle request handler)]
        (add-www-auth-header-if-401 response)))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
