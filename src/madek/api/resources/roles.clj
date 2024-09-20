(ns madek.api.resources.roles
  (:require
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.debug :as debug]
   [madek.api.resources.roles.index :refer [get-index]]
   [madek.api.resources.roles.role :refer [get-role]]
   [madek.api.resources.shared :as shared]))

(def routes
  (cpj/routes
   (cpj/GET "/roles/" [] get-index)
   (cpj/GET "/roles/:id" _ get-role)
   (cpj/ANY "*" _ shared/dead-end-handler)))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
