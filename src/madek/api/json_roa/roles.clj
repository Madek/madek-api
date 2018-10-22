(ns madek.api.json-roa.roles
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn role
  [request response]
  (let [context (:context request)
        role (-> response :body)]
    {:name "Role"
     :self-relation (links/role context (:id role))
     :relations
     {:root (links/root context)}}))

(defn roles
  [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :roles (map :id))]
      {:name "Roles"
       :self-relation (links/roles context query-params)
       :relations {:root (links/root context)}
       :collection
       (conj
         {:relations
          (into {} (map-indexed
                     (fn [i id]
                       [(+ 1 i (pagination/compute-offset query-params))
                        (links/role context id)])
                     ids))}
         (when (seq ids)
           (links/next-link links/roles-path context query-params)
           ))
      })))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
