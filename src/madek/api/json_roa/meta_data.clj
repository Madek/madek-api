(ns madek.api.json-roa.meta-data
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [drtom.logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    {:name "Meta-Data"
     :relations
     (conj
       {:root (links/root context)
        }
       (when-let [id (-> response :body :media_entry_id)]
         {:media-entry (links/media-entry context id)}))
     }))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

