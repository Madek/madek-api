(ns madek.api.resources.keywords
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.keywords.keyword :refer [get-keyword]]
    [madek.api.resources.shared :as shared]
    [madek.api.utils.rdbms :as rdbms]
    ))


(def routes
  (cpj/routes
    (cpj/GET "/keywords/:id" _ get-keyword)
    (cpj/ANY "*" _ shared/dead-end-handler)
    ))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
