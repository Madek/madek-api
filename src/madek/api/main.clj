(ns madek.api.main
  (:gen-class)
  (:require
    [madek.api.constants]
    [madek.api.web]

    [madek.api.utils.config :as config :refer [get-config]]
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.utils.nrepl :as nrepl]

    [clojure.java.jdbc :as jdbc]
    [pg-types.all]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown]
    ))


(defn -main []
  (logbug.thrown/reset-ns-filter-regex #".*madek.*")
  (catcher/snatch
    {:level :fatal
     :throwable Throwable
     :return-fn (fn [e] (System/exit -1))}
    (logging/info 'madek.api.main "initializing ...")
    (madek.api.utils.config/initialize
      {:filenames ["./config/settings.yml"
                   "../config/settings.yml"
                   "./config/settings.local.yml"
                   "../config/settings.local.yml"]})
    (rdbms/initialize (config/get-db-spec :api))
    (nrepl/initialize (-> (get-config) :services :api :nrepl))
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
;(debug/debug-ns 'madek.api.utils.rdbms)
