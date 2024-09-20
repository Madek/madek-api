(ns madek.api.resources.meta-keys
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.debug :as debug]
   [madek.api.pagination :as pagination]
   [madek.api.resources.meta-keys.index :refer [get-index]]
   [madek.api.resources.meta-keys.meta-key :refer [get-meta-key]]
   [madek.api.resources.shared :as shared]
   [madek.api.utils.rdbms :as rdbms]))

(def routes
  (cpj/routes
   (cpj/GET "/meta-keys/" _ get-index)
   (cpj/GET "/meta-keys/:id" _ get-meta-key)
   (cpj/ANY "*" _ shared/dead-end-handler)))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
