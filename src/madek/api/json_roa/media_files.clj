(ns madek.api.json-roa.media-files
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn media-file [request response]
  (let [context (:context request)
        params (:params request)]
    {:name "Media-File"
     :self-relation (links/media-file context (:id params))
     :relations
     {:root (links/root context)
      :data-stream (links/media-file-data-stream context (:id params))
      }}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

