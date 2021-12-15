(ns madek.api.json-roa.group-users
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    [uritemplate-clj.core :refer [uritemplate]]
    ))

(defn users [request response]
  (let [context (:context request)
        query-params (:query-params request)
        group-id (-> request :route-params :group-id)
        ids (->> response :body :users (map :id))]
    (assert group-id)
    {:name "Users"
     :self-relation (links/group-users context :group-id group-id)
     :relations
     {:root (links/root context)}
     :collection
     (conj
       {:relations
        (into {} (map-indexed
                   (fn [i id]
                     [(+ 1 i (pagination/compute-offset query-params))
                      (links/user context id)])
                   ids))}
       (when (seq ids)
         {:next {:href
                 (-> (links/group-users-path
                       context
                       (pagination/next-page-query-query-params query-params))
                     (uritemplate {"group_id" group-id}))}}))}))


;### Debug ####################################################################
;(debug/debug-ns *ns*)
