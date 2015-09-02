(ns madek.api.authorization
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [drtom.logbug.thrown :as thrown]
    [drtom.logbug.catcher :as catcher]
    ))

(defn authorized? [request resource]
  (case (:type resource)
    "MediaEntry" (-> resource
                     :get_metadata_and_previews
                     boolean)
    false))

(defn authorized?! [request resource]
  (or authorized?
      (throw (ex-info "Forbidden"  {:status 403}))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
