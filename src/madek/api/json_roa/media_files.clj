(ns madek.api.json-roa.media-files
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn- previews-map [context response]
  (into {}
        (map #(hash-map (:id %)
                        (links/preview context (:id %)))
             (-> response :body :previews))))

(defn index [request response]
  (logging/debug request)
  (let [context (:context request)
        media-entry-id (-> request :params :media_entry_id)
        query-params (:query-params request)]
    (logging/debug media-entry-id)
    (let [ids (->> response :body :media-files (map :id))]
      {:name "Media-Files"
       :self-relation (links/media-entry-media-files context media-entry-id)
       :collection
       (conj {:relations
              (into {} (map-indexed
                         (fn [i id]
                           [(+ 1 i (pagination/compute-offset query-params))
                            (links/media-file context id)])
                         ids))}
             (when (seq ids)
               (links/next-link links/media-entry-media-files-path context query-params)))
       :relations {:root (links/root context)
                   :media-entry (links/media-entry context media-entry-id)}
       })))

(defn media-file [request response]
  (let [context (:context request)
        params (:params request)]
    {:name "Media-File"
     :self-relation (links/media-file context (:id params))
     :collection {:relations (previews-map context response)}
     :relations
     {:root (links/root context)
      :data-stream (links/media-file-data-stream context (:id params))
      }}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

