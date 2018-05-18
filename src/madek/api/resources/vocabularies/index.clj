(ns madek.api.resources.vocabularies.index
  (:require
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [madek.api.utils.sql :as sql]
    [madek.api.resources.vocabularies.permissions :as permissions]

    [clojure.java.jdbc :as jdbc]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]))

(defn- where-clause
  []
  (let [vocabuary-ids (permissions/accessible-vocabulary-ids)]
    (if (empty? vocabuary-ids)
      [:= :vocabularies.enabled_for_public_view true]
      [:or
        [:= :vocabularies.enabled_for_public_view true]
        [:in :vocabularies.id vocabuary-ids]])))

(defn- base-query
  []
  (-> (sql/select :id)
      (sql/from :vocabularies)
      (sql/merge-where (where-clause))
      sql/format))

(defn- query-index-resources [request]
  (jdbc/query (rdbms/get-ds) (base-query)))

(defn get-index [request]
  (permissions/extract-current-user request)
  (catcher/with-logging {}
    {:body
     {:vocabularies
      (query-index-resources request)}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
