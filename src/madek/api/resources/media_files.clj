(ns madek.api.resources.media-files
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.catcher :as catcher]
    [drtom.logbug.debug :as debug]
    [madek.api.resources.media-files.authorize :as media-files.authorize]
    [madek.api.resources.shared :as shared]
    ))

(defn- get-media-file [request]
  (when-let [media-file (:media-file request)]
    {:status 200
     :body (select-keys media-file [:id :size :created_at :updated_at
                                    :media_entry_id :filename])}))

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
  (-> (cpj/routes
        (cpj/GET "/media-files/:media_file_id" _ #'get-media-file)
        (cpj/ANY "*" _ shared/dead-end-handler))
      media-files.authorize/wrap-authorize
      wrap-find-and-add-media-file))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
(debug/debug-ns *ns*)
