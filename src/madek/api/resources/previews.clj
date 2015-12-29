(ns madek.api.resources.previews
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [logbug.catcher :as catcher]
    [madek.api.pagination :as pagination]
    [madek.api.resources.previews.preview :as preview]
    [madek.api.resources.shared :as shared]
    ))

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
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

