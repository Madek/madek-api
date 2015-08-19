(ns madek.api.resources.shared
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.ring :refer [wrap-handler-with-logging]]
    ))

(def uuid-matcher #"[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}" )

(def dead-end-handler
  (cpj/routes
    (cpj/GET "*" _ {:status 404 :body {:message "404 NOT FOUND"}})
    (cpj/ANY "*" _ {:status 501 :body {:message "501 NOT IMPLEMENTED"}})
    ))


