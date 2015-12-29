(ns madek.api.resources.media-files.media-file
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [madek.api.constants]
    [madek.api.data-streaming :as data-streaming]
    [madek.api.resources.previews.index :as previews]
    ))


(defn- get-media-file [request]
  (when-let [media-file (:media-file request)]
    {:status 200
     :body (conj (select-keys media-file [:id :size :created_at :updated_at
                                          :media_entry_id :filename])
                 {:previews (map #(select-keys % [:id :thumbnail])
                                 (previews/get-index media-file))})}))

(defn- media-file-path [media-file]
  (let [id (:guid media-file)
        [first-char] id]
    (clojure.string/join
      (java.io.File/separator)
      [madek.api.constants/FILE_STORAGE_DIR first-char id])))

(defn get-media-file-data-stream [request]
  (catcher/snatch {}
    (when-let [media-file (:media-file request)]
      (when-let [file-path (media-file-path media-file)]
        (data-streaming/respond-with-file file-path
                                          (:content_type media-file))))))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
