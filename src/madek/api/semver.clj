(ns madek.api.semver
  (:require 
    [clj-commons-exec :as commons-exec]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.catcher :as catcher]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    ))

(defn get-git-commit-id []
  (try 
    (catcher/wrap-with-log-warn
      (-> (commons-exec/sh ["git" "log" "-n" "1" "--format=%h"])
          deref
          :out
          clojure.string/trim))
    (catch Exception _
      "UNKNOWN")))

(defn get-semver []
  (str "3.0.0-beta.1+" (get-git-commit-id))
  )



