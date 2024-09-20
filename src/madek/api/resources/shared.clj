(ns madek.api.resources.shared
  (:require
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.debug :as debug]))

(def uuid-matcher #"[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")

(def dead-end-handler
  (cpj/routes
   (cpj/GET "*" _ {:status 404 :body {:message "404 NOT FOUND"}})
   (cpj/ANY "*" _ {:status 501 :body {:message "501 NOT IMPLEMENTED"}})))

(def internal-keys [:admin_comment])

(defn remove-internal-keys
  [resource]
  (apply dissoc resource internal-keys))
