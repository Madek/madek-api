(ns madek.api.json-roa.auth-info
  (:require
    [uritemplate-clj.core :refer [uritemplate]]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(defn auth-info [request]
  (let [context (:context request)]
    {:name "Root"
     :self-relation (links/auth-info context)
     :relations
     {:root (links/root context)}
     }))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'index)
