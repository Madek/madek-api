(ns madek.api.json-roa.media-files
  (:require
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.json-roa.links :as links]
   [madek.api.pagination :as pagination]))

(defn- previews-map [context response]
  (into {}
        (map #(hash-map (:id %)
                        (links/preview context (:id %)))
             (-> response :body :previews))))

(defn media-file [request response]
  (let [context (:context request)
        params (:params request)]
    {:name "Media-File"
     :self-relation (links/media-file context (:id params))
     :collection {:relations (previews-map context response)}
     :relations
     {:root (links/root context)
      :data-stream (links/media-file-data-stream context (:id params))}}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)

