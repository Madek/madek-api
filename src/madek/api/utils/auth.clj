(ns madek.api.utils.auth
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.utils.sql :as sql]

    [clj-uuid]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [ring.util.codec :refer [url-decode]]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

;### admin check ##############################################################

(defn authorize-admin! [request handler]
  "Checks if the authenticated-entity is an admin by either
  checking (-> request :authenticated-entity :is_admin) if present or performing
  an db query.  If so adds {:is_amdin true} to the requests an calls handler.
  Throws a ExceptionInfo with status 403 otherwise. "
  (handler
    (or
      (if (contains? (-> request :authenticated-entity) :is_admin)
        (when (-> request :authenticated-entity :is_admin) request)
        (when (->> (-> (sql/select [true :is_admin])
                       (sql/from :admins)
                       (sql/merge-where [:= :admins.user_id (-> request :authenticated-entity :id)])
                       sql/format)
                   (jdbc/query (rdbms/get-ds))
                   first :is_admin)
          (assoc-in request [:authenticated-entity :is_admin] true)))
      (throw
        (ex-info
          "Only administrators are allowed to access this resource."
          {:status 403
           :body "Only administrators are allowed to access this resource." })))))

(defn wrap-authorize-admin! [handler]
  (fn [req]
    (authorize-admin! req handler)))


