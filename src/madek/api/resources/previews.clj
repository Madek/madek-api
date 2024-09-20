(ns madek.api.resources.previews
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [madek.api.pagination :as pagination]
   [madek.api.resources.previews.preview :as preview]
   [madek.api.resources.shared :as shared]
   [madek.api.utils.rdbms :as rdbms :refer [get-ds]]))

(defn- query-preview [preview-id]
  ; we wrap this since badly formated media-file-id strings can cause an
  ; exception, note that 404 is in that case a correct response
  (catcher/snatch {}
                  (-> (jdbc/query
                       (get-ds)
                       ["SELECT * FROM previews WHERE id = ?" preview-id])
                      first)))

(defn- wrap-find-and-add-preview
  ([handler] #(wrap-find-and-add-preview % handler))
  ([request handler]
   (when-let [preview-id (-> request :route-params :preview_id)]
     (when-let [preview (query-preview preview-id)]
       (handler (assoc request :preview preview))))))

(def routes
  (-> (cpj/routes
       (cpj/GET "/previews/:preview_id" _ preview/get-preview)
       (cpj/GET "/previews/:preview_id/data-stream" _ preview/get-preview-file-data-stream)
       (cpj/ANY "*" _ shared/dead-end-handler))
      wrap-find-and-add-preview))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
