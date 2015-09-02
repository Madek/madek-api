(ns madek.api.resources.collections.collection
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [drtom.logbug.debug :as debug]
    [madek.api.resources.media-resources.media-resource :as media-resource]
    ))

(def ^:private collection-keys media-resource/media-resource-keys)

(defn get-collection [request]
  (media-resource/get-media-resource request
                                     :table :collections
                                     :mr-keys collection-keys
                                     :mr-type "Collection"))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
