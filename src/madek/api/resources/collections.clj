(ns madek.api.resources.collections
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    ; [madek.api.resources.collections :refer [get-index]]
    [madek.api.resources.collections.collection :refer [get-collection]]
    [madek.api.resources.shared :as shared]
    ))


(def routes
  (cpj/routes
    ; (cpj/GET "/collections/" _ get-index)
    (cpj/GET "/collections/:id" _ get-collection)
    (cpj/ANY "*" _ shared/dead-end-handler)
    ))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
