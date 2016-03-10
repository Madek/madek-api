(ns madek.api.json-roa.root
  (:require
    [madek.api.json-roa.links :as links]
    [logbug.debug :as debug]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]))

(defn build [request]
  (let [context (:context request)]
    {:name "Root"
     :self-relation (links/root context)
     :relations {:auth-info (links/auth-info context)
                 :collections (links/collections context)
                 :collection (links/collection context)
                 :filter-sets (links/filter-sets context)
                 :meta-datum (links/meta-datum context)
                 :media-entries (links/media-entries context)
                 :media-entry (links/media-entry context)
                 :media-file (links/media-file context)
                 :meta-keys (links/meta-keys context)
                 :vocabularies (links/vocabularies context)
                 }}))
