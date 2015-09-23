(ns madek.api.json-roa.meta-data
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [drtom.logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn build-meta-datum-collection-item [request meta-datum]
  (let [context (:context request)
        media-resource (:media-resource request)]
    {(:id meta-datum)
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
                               (into {}))))}


     }))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

