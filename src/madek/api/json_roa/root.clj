(ns madek.api.json-roa.root
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    ))

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
                 :meta-datum-data-stream (links/meta-datum-data-stream context)
                 :meta-datum-role (links/meta-datum-role context)
                 :meta-key (links/meta-key context)
                 :meta-keys (links/meta-keys context)
                 :person (links/person context)
                 :people (links/people context)
                 :role (links/role context)
                 :roles (links/roles context)
                 :users (links/users context)
                 :user (links/user context)
                 :vocabularies (links/vocabularies context)
                 }}))
