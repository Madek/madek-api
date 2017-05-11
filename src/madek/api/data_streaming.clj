(ns madek.api.data-streaming
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [ring.util.response]))

(defn respond-with-file [file-path content-type]
  (if (.exists (clojure.java.io/file file-path))
    (-> (ring.util.response/file-response file-path)
        (ring.util.response/header "X-Sendfile" file-path)
        (ring.util.response/header "content-type" content-type))
    {:status 404 :body {:message "File could not be found!"}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'madek.api.utils.rdbms)
