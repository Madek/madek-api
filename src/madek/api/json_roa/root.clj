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
                 :collection (links/collection context)
                 :collections (links/collections context)
                 :filter-sets (links/filter-sets context)
                 :group (links/group context)
                 :groups (links/groups context)
                 :media-entries (links/media-entries context)
                 :media-entry (links/media-entry context)
                 :media-file (links/media-file context)
                 :meta-datum (links/meta-datum context)
                 :meta-key (links/meta-key context)
                 :meta-keys (links/meta-keys context)
                 :person (links/person context)
                 :people (links/people context)
                 :users (links/users context)
                 :user (links/user context)
                 :vocabularies (links/vocabularies context)
                 }}))
