(ns madek.api.resources.meta-keys.index
  (:require
    [madek.api.utils.rdbms :as rdbms]
    [clojure.java.jdbc :as jdbc]
    [honeysql.sql :refer :all]
    [logbug.catcher :as catcher]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))


(def base-query
  (-> (sql-select :meta-keys.id)
      (sql-from :meta_keys)
      (sql-merge-join :vocabularies
                      [:= :meta_keys.vocabulary_id :vocabularies.id])
      (sql-merge-where [:= :vocabularies.enabled_for_public_view true])))

(defn filter-by-vocabulary [query request]
  (if-let [vocabulary (-> request :query-params :vocabulary)]
    (-> query
        (sql-merge-where [:= :vocabulary_id vocabulary]))
    query))

  (defn build-query [request]
    (-> base-query
        (filter-by-vocabulary request)
        sql-format))

(defn- query-index-resources [request]
  (jdbc/query (rdbms/get-ds)
              (build-query request)))

(defn get-index [request]
  (catcher/with-logging {}
    {:body
     {:meta-keys
      (query-index-resources request)}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
