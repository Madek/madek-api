(ns madek.api.resources.roles
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.resources.roles.index :refer [get-index]]
    [madek.api.resources.roles.role :refer [get-role]]
    [madek.api.resources.shared :as shared]
    ))


(def routes
  (cpj/routes
    (cpj/GET "/roles/" [] get-index)
    (cpj/GET "/roles/:id" _ get-role)
    (cpj/ANY "*" _ shared/dead-end-handler)
    ))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

