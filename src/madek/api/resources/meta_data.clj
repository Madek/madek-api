(ns madek.api.resources.meta-data
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.debug :as debug]
   [madek.api.pagination :as pagination]
   [madek.api.resources.meta-data.index :as meta-data.index]
   [madek.api.resources.meta-data.meta-datum :as meta-datum]
   [madek.api.resources.shared :as shared]
   [madek.api.utils.rdbms :as rdbms]))

(def routes
  (cpj/routes
   (cpj/GET "/media-entries/:media_entry_id/meta-data/" _ meta-data.index/get-index)
   (cpj/GET "/collections/:collection_id/meta-data/" _ meta-data.index/get-index)
   (cpj/GET "/meta-data/:meta_datum_id" _ meta-datum/get-meta-datum)
   (cpj/GET "/meta-data/:meta_datum_id/data-stream" _ meta-datum/get-meta-datum-data-stream)
   (cpj/GET "/meta-data-roles/:meta_datum_id" _ meta-datum/get-meta-datum-role)
   (cpj/ANY "*" _ shared/dead-end-handler)))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
