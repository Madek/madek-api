(ns madek.api.json-roa.collection-media-entry-arcs.core
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clojure.java.jdbc :as jdbc]
    [madek.api.json-roa.links :as json-roa.links]
    [madek.api.json-roa.collection-media-entry-arcs.links :as links]
    [madek.api.pagination :as pagination])
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [clj-logging-config.log4j :as logging-config]
    ))

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
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
(debug/debug-ns *ns*)
