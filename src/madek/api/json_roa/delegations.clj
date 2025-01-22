(ns madek.api.json-roa.delegations
  (:require
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.json-roa.links :as links]
   [madek.api.pagination :as pagination]))

(defn delegation [request response]
  (let [context (:context request)
        delegation (-> response :body)]
    {:name "User"
     :self-relation (links/delegation context (:id delegation))
     :relations
     {:root (links/root context)}}))

(defn delegations [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :delegations (map :id))]
      {:name "Users"
       :self-relation (links/delegations context query-params)
       :relations
       {:root (links/root context)}
       :collection
       (conj
        {:relations
         (into {} (map-indexed
                   (fn [i id]
                     [(+ 1 i (pagination/compute-offset query-params))
                      (links/delegation context id)])
                   ids))}
        (when (seq ids)
          (links/next-link links/delegations-path context query-params)))})))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
