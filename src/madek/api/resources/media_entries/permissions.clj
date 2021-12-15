(ns madek.api.resources.media-entries.permissions
  (:require
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [madek.api.resources.media-resources.permissions :as mr-permissions :only [viewable-by-auth-entity? permission-by-auth-entity?]]
    ))

(defn viewable-by-auth-entity? [resource auth-entity]
  (mr-permissions/viewable-by-auth-entity?
    resource auth-entity :mr-type "media_entry"))

(defn downloadable-by-auth-entity? [resource auth-entity]
  (mr-permissions/permission-by-auth-entity?
    resource auth-entity :get_full_size :mr-type "media_entry"))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
