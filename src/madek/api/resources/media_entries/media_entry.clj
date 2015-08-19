(ns madek.api.resources.media-entries.media-entry
  (:require
    [madek.api.authorization :as authorization]
    [cider-ci.utils.rdbms :as rdbms]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.ring :refer [wrap-handler-with-logging]]
    [honeysql.format :as hsql-format]
    [honeysql.types :as hsql-types]
    [honeysql.helpers :as hsql-helpers]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    ))


(defn authorized? [request media-entry]
  (authorization/authorized? request (assoc media-entry :type "MediaEntry"))
  )

(defn- build-query [id]
  (-> (hsql-helpers/select :*)
      (hsql-helpers/from :media_entries)
      (hsql-helpers/where [:= :id id])
      hsql-format/format
      ))

(defn- query-media-entry [params]
  (->> (:id params)
       build-query
       (jdbc/query (rdbms/get-ds))
       first ))

(def ^:private media-entry-keys
  [:id :created_at :responsible_user_id :creator_id :is_published]
  )

(defn get-media-entry [request]
  (when-let [media-entry (-> request :params query-media-entry)]
    (if (authorized? request media-entry)
      {:body (select-keys media-entry media-entry-keys)}
      {:status 403 })))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
