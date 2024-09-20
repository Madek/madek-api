(ns madek.api.json-roa.vocabularies
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.json-roa.links :as links]
   [madek.api.pagination :as pagination]
   [madek.api.utils.rdbms :as rdbms]))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    (let [ids (->> response :body :vocabularies (map :id))]
      {:name "Vocabularies"
       :self-relation (links/vocabularies context query-params)
       :relations
       {:root (links/root context)}
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
     :relations {:meta-keys (links/meta-keys context {:vocabulary vocabulary-id})}}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
