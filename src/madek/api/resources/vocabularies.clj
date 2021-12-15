(ns madek.api.resources.vocabularies
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [madek.api.resources.vocabularies.index :refer [get-index]]
    [madek.api.resources.vocabularies.vocabulary :refer [get-vocabulary]]
    [madek.api.utils.rdbms :as rdbms]
    ))

(def routes
  (cpj/routes
    (cpj/GET "/vocabularies/" _ get-index)
    (cpj/GET "/vocabularies/:id" _ get-vocabulary)
    (cpj/ANY "*" _ shared/dead-end-handler)
    ))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
