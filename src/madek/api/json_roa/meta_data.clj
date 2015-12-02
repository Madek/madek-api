(ns madek.api.json-roa.meta-data
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn build-meta-datum-collection-item [request meta-datum]
  (let [context (:context request)
        media-resource (:media-resource request)]
    {(:meta_key_id meta-datum)
     (links/meta-datum context meta-datum)}))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    {:name "Meta-Data"
     :relations (conj {:root (links/root context) }
                      (when-let [id (-> response :body :media_entry_id)]
                        {:media-entry (links/media-entry context id)}))
     :collection
     {:relations (merge {}
                        (when-let [meta-data (-> response :body :meta-data)]
                          (->> meta-data
                               (map #(build-meta-datum-collection-item request %))
                               (into {}))))}}))

(defn meta-datum [request response]
  (let [context (:context request)
        meta-datum-type (-> response :body :type)]
    (conj {:name "Meta-Datum"
           :relations {:root (links/root context)
                       :meta-key (links/meta-key
                                   context (-> response :body :meta_key_id))
                       :media-entry (links/media-entry
                                      context (-> response :body :media_entry_id))}}
          (when-not (or (= meta-datum-type "MetaDatum::Text")
                        (= meta-datum-type "MetaDatum::TextDate"))
            {:collection
             {:relations
              (into {}
                    (map #(hash-map % ((case meta-datum-type
                                         "MetaDatum::People" links/person
                                         "MetaDatum::Keywords" links/keyword-term
                                         "MetaDatum::Licenses" links/license)
                                       context %))
                         (map :id (-> response :body :value))))}}))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

