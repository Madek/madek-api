(ns madek.api.data-streaming
  (:require
    [ring.util.response]))

(defn respond-with-file [file-path content-type]
  (-> (ring.util.response/file-response file-path)
      (ring.util.response/header "X-Sendfile" file-path)
      (ring.util.response/header "content-type" content-type)))
