(ns madek.api.resources.collections.permissions
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.thrown :as thrown]
    [logbug.catcher :as catcher]
    [madek.api.resources.media-resources.permissions :as mr-permissions]
    ))

(defn viewable-by-auth-entity? [resource auth-entity]
  (mr-permissions/viewable-by-auth-entity?
    resource auth-entity :mr-type "collection"))
