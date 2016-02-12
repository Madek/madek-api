(ns madek.api.json-roa.collections
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn collection [request response]
  (let [context (:context request)
        params (:params request)
        id (-> response :body :id)]
    {:name "Collection"
     :self-relation (links/collection context (:id params))
     :relations
     {:root (links/root context)
      :meta-data (links/collection-meta-data context (:id params))
      :media-entries (links/media-entries context {:collection_id id})
      }}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

