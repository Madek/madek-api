(ns madek.api.resources.meta-keys
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.meta-keys.meta-key :refer [get-meta-key]]
    [madek.api.resources.meta-keys.index :refer [get-index]]
    [madek.api.resources.shared :as shared]
    ))

(def routes
  (cpj/routes
    (cpj/GET "/meta-keys/" _ get-index)
    (cpj/GET "/meta-keys/:id" _ get-meta-key)
    (cpj/ANY "*" _ shared/dead-end-handler)
    ))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
