(ns madek.api.semver
  (:require
   [clj-commons-exec :as commons-exec]
   [clojure.tools.logging :as logging]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]))

(defn get-git-commit-id []
  (try
    (catcher/with-logging {}
      (-> (commons-exec/sh ["git" "log" "-n" "1" "--format=%h"])
          deref
          :out
          clojure.string/trim))
    (catch Exception _
      "UNKNOWN")))

(defn get-semver []
  (str "3.0.0-beta.1+" (get-git-commit-id)))
