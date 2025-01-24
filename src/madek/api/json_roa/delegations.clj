(ns madek.api.json-roa.delegations
  (:require
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.json-roa.links :as links]
   [madek.api.pagination :as pagination]))

(defn delegation [request response]
  (let [context (:context request)
        delegation (-> response :body)]
    {:name "Delegation"
     :self-relation (links/delegation context (:id delegation))
     :relations
     {:root (links/root context)}}))

(defn delegations [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :delegations (map :id))]
      {:name "Delegations"
       :self-relation (links/delegations context query-params)
       :relations
       {:root (links/root context)}})))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
