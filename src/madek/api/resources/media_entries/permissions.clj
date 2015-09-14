(ns madek.api.resources.media-entries.permissions
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [drtom.logbug.thrown :as thrown]
    [drtom.logbug.catcher :as catcher]
    [madek.api.resources.media-resources.permissions :as mr-permissions]
    ))

(defn viewable-by-auth-entity? [resource auth-entity]
  (mr-permissions/viewable-by-auth-entity?
    resource auth-entity :mr-type "media_entry"))
