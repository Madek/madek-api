(ns madek.api.resources.media-entries.advanced-filter
  (:require
   [clojure.tools.logging :as logging]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [madek.api.resources.media-entries.advanced-filter.media-files :as media-files]
   [madek.api.resources.media-entries.advanced-filter.meta-data :as meta-data]
   [madek.api.resources.media-entries.advanced-filter.permissions :as permissions]
   [madek.api.utils.sql :as sql]))

(defn filter-by [sqlmap filter-map]
  (-> sqlmap
      (media-files/sql-filter-by (:media_files filter-map))
      (permissions/sql-filter-by (:permissions filter-map))
      (meta-data/sql-filter-by (:meta_data filter-map))
      (meta-data/sql-search-through-all (:search filter-map))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'build-query)
