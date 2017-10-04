(ns madek.api.json-roa.people
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn person [request response]
  (let [context (:context request)
        person (-> response :body)]
    {:name "Person"
     :self-relation (links/person context (:id person))
     :relations
     {:root (links/root context)}}))

(defn people [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :people (map :id))]
      {:name "People"
       :self-relation (links/people context query-params)
       :relations
       {:root (links/root context)}
       :collection
       (conj
         {:relations
          (into {} (map-indexed
                     (fn [i id]
                       [(+ 1 i (pagination/compute-offset query-params))
                        (links/person context id)])
                     ids))}
         (when (seq ids)
           (links/next-link links/people-path context query-params)
           ))})))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)


