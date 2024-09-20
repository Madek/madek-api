(ns madek.api.data-streaming
  (:require
   [clojure.tools.logging :as logging]
   [ring.util.response]))

(defn respond-with-file [file-path content-type]
  (if (.exists (clojure.java.io/file file-path))
    (-> (ring.util.response/file-response file-path)
        (ring.util.response/header "X-Sendfile" file-path)
        (ring.util.response/header "content-type" content-type))
    {:status 404 :body {:message "File could not be found!"}}))

;### Debug ####################################################################
;(debug/debug-ns 'madek.api.utils.rdbms)
