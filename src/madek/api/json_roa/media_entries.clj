(ns madek.api.json-roa.media-entries
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [drtom.logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
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
           (links/next-rel
             (fn [query-params]
               (links/media-entries-path context query-params))
             query-params)))})))

(defn media-entry [request response]
  (let [context (:context request)
        params (:params request)]
    {:name "Media-Entry"
     :self-relation (links/media-entry context (:id params))
     :relations
     {:root (links/root context)
      :meta-data (links/media-entry-meta-data context (:id params))
      }}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

