(ns madek.api.resources.vocabularies
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.vocabularies.vocabulary :refer [get-vocabulary]]
    [madek.api.resources.vocabularies.index :refer [get-index]]
    [madek.api.resources.shared :as shared]
    ))

(def routes
  (cpj/routes
    (cpj/GET "/vocabularies/" _ get-index)
    (cpj/GET "/vocabularies/:id" _ get-vocabulary)
    (cpj/ANY "*" _ shared/dead-end-handler)
    ))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
