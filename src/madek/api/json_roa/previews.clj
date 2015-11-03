(ns madek.api.json-roa.previews
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn preview [request response]
  (let [context (:context request)
        params (:params request)]
    {:name "Preview"
     :self-relation (links/preview context (:id params))
     :relations
     {:root (links/root context)
      :media-file (links/media-file context
                                    (-> response :body :media_file_id))
      :data-stream (links/preview-file-data-stream context (:id params))}}))

;### Debug ####################################################################
; (logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
