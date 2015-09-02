(ns madek.api.resources
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.ring :refer [wrap-handler-with-logging]]
    [madek.api.resources.auth-info :as auth-info]
    [madek.api.resources.collections :as collections]
    [madek.api.resources.media-entries :as media-entries]
    [madek.api.resources.meta-data :as meta-data]
    [madek.api.resources.shared :as shared]
    ))


(defn wrap-api-routes [default-handler]
  (cpj/routes
    (cpj/ANY "/media-entries/:media_entry_id/meta-data/" _ meta-data/routes)
    (cpj/ANY "/media-entries*" _ media-entries/routes)
    (cpj/ANY "/collections*" _ collections/routes)
    (cpj/GET "/auth-info" _ auth-info/routes)
    (cpj/ANY "*" _ default-handler)
    ))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

