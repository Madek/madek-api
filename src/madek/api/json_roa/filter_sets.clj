(ns madek.api.json-roa.filter-sets
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :filter-sets (map :id))]
      {:name "FilterSets"
       :self-relation (links/filter-sets context query-params)
       :relations
       {:root (links/root context)}
       :collection
       (conj
         {:relations
          (into {} (map-indexed
                     (fn [i id]
                       [(+ 1 i (pagination/compute-offset query-params))
                        (links/filter-set context id)])
                     ids))}
         (when (seq ids)
           (links/next-link links/filter-sets-path context query-params)
           ))})))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
