(ns madek.api.resources.meta-data
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [madek.api.resources.meta-data.index :as meta-data.index]
    [madek.api.resources.meta-data.meta-datum :as meta-datum]
    ))

(def routes
  (cpj/routes
    (cpj/GET "/media-entries/:media_entry_id/meta-data/" _ meta-data.index/get-index)
    (cpj/GET "/collections/:collection_id/meta-data/" _ meta-data.index/get-index)
    (cpj/GET "/meta-data/:meta_datum_id" _ meta-datum/get-meta-datum)
    (cpj/GET "/meta-data/:meta_datum_id/data-stream" _ meta-datum/get-meta-datum-data-stream)
    (cpj/GET "/meta-data-roles/:meta_datum_id" _ meta-datum/get-meta-datum-role)
    (cpj/ANY "*" _ shared/dead-end-handler)
    ))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
