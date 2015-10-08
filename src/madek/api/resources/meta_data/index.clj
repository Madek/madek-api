(ns madek.api.resources.meta-data.index
  (:require
    [madek.api.authorization :as authorization]
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [logbug.ring :refer [wrap-handler-with-logging]]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [logbug.catcher :as catcher]
    ))

(defn get-meta-data [media-resource]
  (case (:type media-resource)
    "MediaEntry" (jdbc/query (get-ds)
                             [(str "SELECT id, type, meta_key_id from meta_data"
                                   "  WHERE media_entry_id = ? ")
                              (:id media-resource)])))

(defn get-index [request]
  (if-let [media-resource (:media-resource request)]
    (let [meta-data (get-meta-data media-resource)]
      {:body
       (conj
         {:meta-data meta-data}
         (case (:type media-resource)
           "MediaEntry" {:media_entry_id (:id media-resource)}))})))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
