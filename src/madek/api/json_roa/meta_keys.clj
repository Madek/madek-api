(ns madek.api.json-roa.meta-keys
  (:require
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.json-roa.links :as links]
   [madek.api.pagination :as pagination]))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)
        ids (->> response :body :meta-keys (map :id))]
    {:name "Meta-Keys"
     :self-relation (links/meta-keys context query-params)
     :relations {:root (links/root context)}
     :collection
     {:relations
      (into {} (map-indexed
                (fn [i id]
                  [(+ 1 i (pagination/compute-offset query-params))
                   (links/meta-key context id)])
                ids))}}))

(defn meta-key [request response]
  (let [context (:context request)
        params (:params request)
        vocabulary-id (-> response :body :vocabulary_id)
        id (-> response :body :id)]
    {:name "Meta-Key"
     :self-relation (links/meta-key context id)
     :relations
     {:root (links/root context)
      :vocabulary (links/vocabulary context vocabulary-id)}}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
