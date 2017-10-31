(ns madek.api.json-roa.users
  (:require
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(defn user [request response]
  (let [context (:context request)
        user (-> response :body)]
    {:name "User"
     :self-relation (links/user context (:id user))
     :relations
     {:root (links/root context)
      :person (links/person context (:person_id  user))
      }}))

(defn users [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :users (map :id))]
      {:name "Users"
       :self-relation (links/users context query-params)
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
           (links/next-link links/users-path context query-params)
           ))})))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
