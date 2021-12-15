(ns madek.api.resources.filter-sets
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.filter-sets.index :refer [get-index]]
    [madek.api.resources.shared :as shared]
    [madek.api.utils.rdbms :as rdbms]
    ))

(def routes
  (cpj/routes
    (cpj/GET "/filter-sets/" _ get-index)
    ; (cpj/GET "/filter-sets/:id" _ get-filter-set)
    (cpj/ANY "*" _ shared/dead-end-handler)))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
