(ns madek.api.json-roa.collection-media-entry-arcs.core
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.collection-media-entry-arcs.links :as links]
    [madek.api.json-roa.links :as json-roa.links]
    [madek.api.pagination :as pagination]
    [madek.api.utils.rdbms :as rdbms]))

(defn- collection-relations [context query-params ids]
  (->> ids
       (map-indexed
         (fn [i id]
           [(+ 1 i (pagination/compute-offset query-params))
            (links/collection-media-entry-arc context id)]))
       (into {})))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :collection-media-entry-arcs (map :id))]
      {:name "Collection-Media-Entry-Arcs"
       :self-relation (links/collection-media-entry-arcs context query-params)
       :relations {:root (json-roa.links/root context)}
       :collection
       (conj
         {:relations (collection-relations context query-params ids)}
         (when (seq ids)
           (json-roa.links/next-link
             links/collection-media-entry-arcs-path context query-params)
           ))})))

(defn item [request response]
  (let [context (:context request)
        id (-> response :body :id)]
    {:name "Collection-Media-Entry-Arc"
     :self-relation (links/collection-media-entry-arc context id)
     :relations
     {:root (json-roa.links/root context)
      :media-entry (json-roa.links/media-entry
                     context (-> response :body :media_entry_id))
      :collection (json-roa.links/collection
                     context (-> response :body :collection_id))}}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
