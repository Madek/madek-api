(ns madek.api.resources.media-files.authorize
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [logbug.catcher :as catcher]
    [madek.api.resources.shared :as shared]
    ))

(defn wrap-authorize
  ([handler] #(wrap-authorize % handler))
  ([request handler]
   ; TODO: implement this
   (logging/warn "AUTHORIZE FOR MEDIA-FILES IS PENDING" {:request request})
   (handler request)))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
(debug/debug-ns *ns*)
