(ns madek.api.json-roa.vocabularies
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [clojure.java.jdbc :as jdbc]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :vocabularies (map :id))]
      {:name "Vocabularies"
       :self-relation (links/vocabularies context query-params)
       :relations
       {:root (links/root context)
        }
       :collection
       {:relations
        (into {} (map-indexed
                   (fn [i id]
                     [(+ 1 i (pagination/compute-offset query-params))
                      (links/vocabulary context id)])
                   ids))}})))

(defn vocabulary [request response]
  (let [context (:context request)
        params (:params request)
        vocabulary-id (:id params)]
    {:name "Vocabulary"
     :self-relation (links/vocabulary context vocabulary-id)
     :relations {:meta-keys (links/meta-keys context {:vocabulary vocabulary-id})
                 }}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

