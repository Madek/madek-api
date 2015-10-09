(ns madek.api.resources.media-files
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.ring]
    [madek.api.resources.media-files.authorization :as media-files.authorization]
    [madek.api.resources.media-files.media-file :as media-file]
    [madek.api.resources.shared :as shared]
    ))

;##############################################################################

(defn- query-media-file [media-file-id]
  ; we wrap this since badly formated media-file-id strings can cause an
  ; exception, note that 404 is in that case a correct response
  (catcher/wrap-with-suppress-and-log-warn
    (-> (jdbc/query
          (get-ds)
          ["SELECT * FROM media_files WHERE id = ?" media-file-id])
        first)))

(defn- wrap-find-and-add-media-file
  ([handler] #(wrap-find-and-add-media-file % handler))
  ([request handler]
   (when-let [media-file-id (-> request :route-params :media_file_id)]
     (when-let [media-file (query-media-file media-file-id)]
       (handler (assoc request :media-file media-file))))))

;##############################################################################

(def routes
  (logbug.ring/->
    (cpj/routes
      (cpj/GET "/media-files/:media_file_id" _
               #'media-file/get-media-file-row)
      (cpj/GET "/media-files/:media_file_id/data-stream" _
               #'media-file/get-media-file-data-stream)
      (cpj/ANY "*" _ shared/dead-end-handler))
    media-files.authorization/wrap-authorize
    wrap-find-and-add-media-file))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
