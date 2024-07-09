(ns madek.api.resources.media-files
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [clojure.string]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug :refer [I> I>>]]
    [logbug.ring :as logbug-ring :refer [wrap-handler-with-logging]]
    [madek.api.resources.media-files.authorization :as media-files.authorization]
    [madek.api.resources.media-files.media-file :as media-file]
    [madek.api.resources.shared :as shared]
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [madek.api.utils.sql :as sql]
    [madek.api.resources.media-files.common :refer [media-entry-undeleted-exists-cond]]
    ))

;##############################################################################


(defn query [media-file-id]
  (-> (sql/select :*)
      (sql/from :media_files)
      (sql/merge-where [:= :id media-file-id])
      (sql/merge-where (media-entry-undeleted-exists-cond media-file-id))
      ))

(defn- query-media-file [media-file-id]
  ; we wrap this since badly formated media-file-id strings can cause an
  ; exception, note that 404 is in that case a correct response
  (catcher/snatch
    {}
    (-> (jdbc/query
          (get-ds)
          (query media-file-id))
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
                 (media-files.authorization/wrap-authorize
                   #'media-file/get-media-file-data-stream :get_full_size))
        (cpj/ANY "*" _ shared/dead-end-handler))
      (media-files.authorization/wrap-authorize :get_metadata_and_previews)
      wrap-find-and-add-media-file))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
