(ns madek.api.resources.collections.permissions
  (:require
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.thrown :as thrown]
    [madek.api.resources.media-resources.permissions :as mr-permissions]
    ))

(defn viewable-by-auth-entity? [resource auth-entity]
  (mr-permissions/viewable-by-auth-entity?
    resource auth-entity :mr-type "collection"))
