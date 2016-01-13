(ns madek.api.main
  (:require
    [cider-ci.utils.config :as config :refer [get-config]]
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown]
    [madek.api.constants]
    [madek.api.web]
    [pg-types.all]
    ))


(defn -main []
  (logbug.thrown/reset-ns-filter-regex #".*madek.*")
  (catcher/with-logging {}
    (logging/info 'madek.api.main "initializing ...")
    (cider-ci.utils.config/initialize
      {:filenames ["./config/settings.yml"
                   "../config/settings.yml"
                   "./config/settings.local.yml"
                   "../config/settings.local.yml"]})
    (rdbms/initialize (config/get-db-spec :api))
    (madek.api.web/initialize)
    (madek.api.constants/initialize (get-config))
    (logging/info 'madek.api.main "... initialized")))

(defn- reload []
  (require 'madek.api.main :reload-all)
  (rdbms/initialize (config/get-db-spec :api))
  (madek.api.web/start-server))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.rdbms)
