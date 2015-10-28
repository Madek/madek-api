(ns madek.api.resources.licenses
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.licenses.license :refer [get-license]]
    [madek.api.resources.shared :as shared]
    ))


(def routes
  (cpj/routes
    (cpj/GET "/licenses/:id" _ get-license)
    (cpj/ANY "*" _ shared/dead-end-handler)
    ))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
