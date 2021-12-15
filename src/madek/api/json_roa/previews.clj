(ns madek.api.json-roa.previews
  (:require
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
;(debug/debug-ns *ns*)
