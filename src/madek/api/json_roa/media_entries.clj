(ns madek.api.json-roa.media-entries
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.collection-media-entry-arcs.links :as collection-media-entry-arcs.links]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    [madek.api.utils.rdbms :as rdbms]
    ))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :media-entries (map :id))]
      {:name "Media-Entries"
       :self-relation (links/media-entries context query-params)
       :relations
       {:root (links/root context)
        }
       :collection
       (conj
         {:relations
          (into {} (map-indexed
                     (fn [i id]
                       [(+ 1 i (pagination/compute-offset query-params))
                        (links/media-entry context id)])
                     ids))}
         (when (seq ids)
           (links/next-link links/media-entries-path context query-params)
           ))})))

(defn get-first-media-file-id [media-entry-id]
  (-> (jdbc/query
        (rdbms/get-ds)
        [(str "SELECT id FROM media_files WHERE media_entry_id = ?"
              " ORDER BY created_at ASC LIMIT 1") media-entry-id])
      first :id))

(defn media-entry [request response]
  (let [context (:context request)
        params (:params request)
        media-entry-id (:id params)]
    {:name "Media-Entry"
     :self-relation (links/media-entry context media-entry-id)
     :relations (merge {:root (links/root context)
                        :meta-data (links/media-entry-meta-data context media-entry-id)
                        :collection-media-entry-arcs
                        (collection-media-entry-arcs.links/collection-media-entry-arcs
                          context {:media_entry_id media-entry-id})}
                       (if-let [media-file-id (get-first-media-file-id media-entry-id)]
                         {:media-file (links/media-file context media-file-id) } {})
                       )}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
