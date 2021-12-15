(ns madek.api.json-roa.auth-info
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    [uritemplate-clj.core :refer [uritemplate]]
    ))

(defn auth-info [request]
  (let [context (:context request)]
    {:name "Root"
     :self-relation (links/auth-info context)
     :relations
     {:root (links/root context)}
     }))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'index)
