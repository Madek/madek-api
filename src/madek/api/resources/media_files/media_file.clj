(ns madek.api.resources.media-files.media-file
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [madek.api.constants]
    [ring.util.response]
    ))


(defn- get-media-file-row [request]
  (when-let [media-file (:media-file request)]
    {:status 200
     :body (select-keys media-file [:id :size :created_at :updated_at
                                    :media_entry_id :filename])}))

(defn- media-file-path [media-file]
  (let [id (:guid media-file)
        [first-char] id]
    (clojure.string/join
      (java.io.File/separator)
      [madek.api.constants/FILE_STORAGE_DIR first-char id])))

(defn get-media-file-data-stream [request]
  (catcher/wrap-with-suppress-and-log-warn
    (when-let [media-file (:media-file request)]
      (when-let [file-path (media-file-path media-file)]
        (-> (ring.util.response/file-response file-path)
            (ring.util.response/header "X-Sendfile" file-path)
            (ring.util.response/header "content-type"
                                       (:content_type media-file)))))))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
