(ns madek.api.resources.media-files.authorization
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [logbug.catcher :as catcher]
    [madek.api.resources.media-entries.permissions :as me-permissions]
    [madek.api.resources.shared :as shared]
    ))

(defn wrap-authorize
  ([handler] #(wrap-authorize % handler))
  ([request handler]
   (let [media-entry-id (get-in request [:media-file :media_entry_id])
         media-entry (-> (jdbc/query (get-ds)
                                     [(str "SELECT * FROM media_entries WHERE id = ?")
                                      media-entry-id]) first)]
     (if (:get_full_size media-entry)
       (handler request)
       (if-let [auth-entity (:authenticated-entity request)]
         (if (me-permissions/downloadable-by-auth-entity? media-entry auth-entity)
           (handler request)
           {:status 403})
         {:status 401})))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
