(ns madek.api.json-roa.collections
  (:require
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.json-roa.links :as links]
   [madek.api.pagination :as pagination]
   [uritemplate-clj.core :refer [uritemplate]]))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :collections (map :id))]
      {:name "Collections"
       :self-relation (links/collections context query-params)
       :relations
       {:root (links/root context)}
       :collection
       (conj
        {:relations
         (into {} (map-indexed
                   (fn [i id]
                     [(+ 1 i (pagination/compute-offset query-params))
                      (links/collection context id)])
                   ids))}
        (when (seq ids)
          (links/next-link links/collections-path context query-params)))})))

(defn collection [request response]
  (let [context (:context request)
        params (:params request)
        id (-> response :body :id)]
    {:name "Collection"
     :self-relation (links/collection context (:id params))
     :relations
     {:root (links/root context)
      :meta-data (links/collection-meta-data context (:id params))
      :media-entries (links/media-entries context {:collection_id id})
      :collection-media-entry-arcs (links/collection-media-entry-arcs
                                    context {:collection_id id})
      :collections (links/collections context {:collection_id id})}}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'index)
