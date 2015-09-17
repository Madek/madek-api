(ns madek.api.resources.meta-data
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [madek.api.resources.meta-data.index :as meta-data.index]
    ))

(def routes
  (cpj/routes
    (cpj/GET "/media-entries/:media_entry_id/meta-data/" _ meta-data.index/get-index)
    (cpj/ANY "*" _ shared/dead-end-handler)
    ))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
