(ns madek.api.resources.media-resources.media-resource
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.ring :refer [wrap-handler-with-logging]]
    [honeysql.sql :refer :all]
    [madek.api.authorization :as authorization]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    ))

(defn build-query [table id]
  (-> (sql-select :*)
      (sql-from table)
      (sql-where [:= :id id])
      sql-format))

(defn query-media-resource [params table]
  (->> (:id params)
       (build-query table)
       (jdbc/query (rdbms/get-ds))
       first))

(defn authorized? [request media-resource mr-type]
  (authorization/authorized? request (assoc media-resource :type mr-type)))

(def media-resource-keys
  [:id :created_at :responsible_user_id :creator_id])

(defn- public? [resource]
  (-> resource :get_metadata_and_previews boolean))

(defn get-media-resource [request & {:keys [table mr-keys mr-type]}]
  " get media resource according to the following rules:
  * if public -> 200
  * if not public:
  ** and user is not authenticated -> 401
  ** and user is authenticated:
  *** and authorized -> 200
  *** but not authorized -> 403
  "
  (when-let [media-resource (-> request
                             :params
                             (query-media-resource table))]
    (if (public? media-resource)
      {:body (select-keys media-resource mr-keys)}
      (if-let [auth-entity (:authenticated-entity request)]
        (if (authorized? auth-entity media-resource mr-type)
          {:body (select-keys media-resource mr-keys)}
          {:status 403})
        {:status 401}))))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
