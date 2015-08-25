(ns madek.api.json-roa.root
  (:require
    [madek.api.json-roa.links :as links]
    [drtom.logbug.debug :as debug]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]))

(defn build [request]
  (let [context (:context request)]
    {:name "Root"
     :self-relation (links/root context)
     :relations {:media-entries (links/media-entries context)
                 :media-entry (links/media-entry context)
                 :auth-info (links/auth-info context)
                 }}))
