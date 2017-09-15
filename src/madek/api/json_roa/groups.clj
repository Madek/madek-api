(ns madek.api.json-roa.groups
  (:require
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(defn group [request response]
  (let [context (:context request)
        group (-> response :body)]
    {:name "Group"
     :self-relation (links/group context (:id group))
     :relations
     {:root (links/root context)}}))

(defn groups [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :groups (map :id))]
      {:name "Groups"
       :self-relation (links/groups context query-params)
       :relations
       {:root (links/root context)}
       :collection
       (conj
         {:relations
          (into {} (map-indexed
                     (fn [i id]
                       [(+ 1 i (pagination/compute-offset query-params))
                        (links/group context id)])
                     ids))}
         (when (seq ids)
           (links/next-link links/groups-path context query-params)
           ))})))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
