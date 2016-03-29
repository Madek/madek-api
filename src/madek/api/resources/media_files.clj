(ns madek.api.resources.media-files
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [madek.api.resources.media-files.authorization :as media-files.authorization]
    [madek.api.resources.media-files.media-file :as media-file]
    [madek.api.resources.shared :as shared]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug :refer [I> I>>]]
    [logbug.ring :as logbug-ring :refer [wrap-handler-with-logging]]
    ))

;##############################################################################

(defn- query-media-file [media-file-id]
  ; we wrap this since badly formated media-file-id strings can cause an
  ; exception, note that 404 is in that case a correct response
  (catcher/snatch {}
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
  (I>  wrap-handler-with-logging
      (cpj/routes
        (cpj/GET "/media-files/:media_file_id" _
                 #'media-file/get-media-file)
        (cpj/GET "/media-files/:media_file_id/data-stream" _
                 #'media-file/get-media-file-data-stream)
        (cpj/ANY "*" _ shared/dead-end-handler))
      media-files.authorization/wrap-authorize
      wrap-find-and-add-media-file))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
