(ns madek.api.json-roa.users
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
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
;(debug/debug-ns *ns*)
